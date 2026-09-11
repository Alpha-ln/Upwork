package com.example.upwork.network

import com.example.upwork.model.ActivationCode
import com.example.upwork.model.Video
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getUserRole(uid: String): String? {
        return try {
            val docRef = db.collection("users").document(uid)
            val snapshot = docRef.get().await()
                if (!snapshot.exists()){
                    return null
                }
            val role = snapshot.getString("role")
            role
        }catch (e: Exception){
            null
        }
    }
    suspend fun saveCodes(codeMap: Map<String, ActivationCode>): Boolean {
        return try {
            val batch = db.batch()
            codeMap.forEach { (codeStr, activationCode) ->
                val ref = db.collection("codes").document(codeStr)
                batch.set(ref, activationCode)
            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }
    suspend fun checkSessionActivation(studentId: String, videoId: String): Boolean {
        return try {
            val result = db.collection("codes")
                .whereEqualTo("videoId", videoId)
                .whereEqualTo("usedByStudentId", studentId)
                .get().await()
            !result.isEmpty
        }catch (e: Exception) {
            false
        }
    }
    suspend fun activateCode(code: String, studentId: String, videoId: String): Result<Unit> {
        return try {
            val studentRef = db.collection("students").document(studentId)
            val codeRef = db.collection("codes").document(code)
            val snapshot = codeRef.get().await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("Code not found"))
            }
            val codeData = snapshot.toObject(ActivationCode::class.java) ?: return Result.failure(
                Exception("Missing required fields")
            )
            if (codeData.status != "active") {
                    return Result.failure(Exception("Invalid code or expired"))
                }
                if (codeData.videoId != videoId) {
                    return Result.failure(Exception("Wrong video"))
                }
            db.runTransaction { transaction ->
                transaction.update(codeRef,
                    mapOf(
                        "status" to "used",
                        "usedByStudentId" to studentId,
                        "usedAt" to Timestamp.now()
                    ))
                transaction.update(studentRef,
                    mapOf(
                        "unlockedVideoIds" to FieldValue.arrayUnion(videoId)
                    ))
            }
            Result.success(Unit)

        }catch (e: Exception){
            return Result.failure(e)
        }
    }
    // Video implementation
    suspend fun addNewVideo(video: Video): Boolean {
        return try {
            db.collection("videos").document(video.videoId).set(video).await()
            true
        } catch (e: Exception){
            false
        }
    }
    suspend fun deleteVideo(video: Video): Boolean {
        return try {
            db.collection("videos").document(video.videoId).delete().await()
            true
        }catch (e: Exception){
            false
        }
    }
    suspend fun getAllVideos(): List<Video> {
        return try {
            db.collection("videos").get().await().toVideoList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRecentVideos(): List<Video> {
        return try {
            db.collection("videos")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5).get().await().toVideoList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUnlockedVideos(studentId: String): List<Video> {
        return try {
            val studentRef = db.collection("students").document(studentId)
            val snapshot = studentRef.get().await()
            if (!snapshot.exists()) {
                return emptyList()
            }
            val unlockedVideoIds = snapshot.get("unlockedVideoIds") as? List<String>
            if (unlockedVideoIds.isNullOrEmpty()) {
                return emptyList()
            }
            db.collection("videos")
                .whereIn("videoId", unlockedVideoIds)
                .get().await().toVideoList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun QuerySnapshot.toVideoList(): List<Video> {
        return this.documents.mapNotNull { it.toObject(Video::class.java) }
    }
}
