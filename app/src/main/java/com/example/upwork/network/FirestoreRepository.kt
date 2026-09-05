package com.example.upwork.network

import android.util.Log
import com.example.upwork.model.Code
import com.example.upwork.model.Instructor
import com.example.upwork.model.Student
import com.example.upwork.model.Video
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "FirestoreRepository"

    // --- Instructor Methods ---
    suspend fun getInstructors(): List<Instructor> {
        return try {
            val snapshot = db.collection("Instructors")
                .whereEqualTo("verified", true)
                .get()
                .await()
            snapshot.toObjects(Instructor::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting instructors: ", e)
            emptyList()
        }
    }

    suspend fun getPendingInstructors(): List<Instructor> {
        return try {
            val snapshot = db.collection("Instructors")
                .whereEqualTo("verified", false)
                .get()
                .await()
            snapshot.toObjects(Instructor::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pending instructors: ", e)
            emptyList()
        }
    }

    suspend fun approveInstructor(instructorId: String): Boolean {
        return try {
            db.collection("Instructors").document(instructorId)
                .update("verified", true)
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error approving instructor: ", e)
            false
        }
    }

    // --- Video Methods ---
    suspend fun getVideosForInstructor(instructorId: String): List<Video> {
        return try {
            val snapshot = db.collection("Videos")
                .whereEqualTo("instructorId", instructorId)
                .get()
                .await()
            snapshot.toObjects(Video::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting videos: ", e)
            emptyList()
        }
    }

    suspend fun getVideoById(videoId: String): Video? {
        return try {
            // Using document(videoId) is the most efficient way if videoId is the Document ID
            val doc = db.collection("Videos").document(videoId).get().await()
            doc.toObject(Video::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting video details: ", e)
            null
        }
    }

    suspend fun addVideo(video: Video): Boolean {
        return try {
            // We use videoId as the Document ID to ensure uniqueness and fast lookup
            db.collection("Videos").document(video.videoId).set(video).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding video: ", e)
            false
        }
    }

    suspend fun deleteVideo(videoId: String): Boolean {
        return try {
            db.collection("Videos").document(videoId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting video: ", e)
            false
        }
    }

    // --- Student Methods ---
    suspend fun getStudents(): List<Student> {
        return try {
            val snapshot = db.collection("Students").get().await()
            snapshot.toObjects(Student::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting students: ", e)
            emptyList()
        }
    }

    suspend fun addStudent(student: Student): Boolean {
        return try {
            db.collection("Students").document(student.studentId).set(student).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding student: ", e)
            false
        }
    }

    // --- Code Generation Methods ---
    suspend fun saveCodes(codes: List<Code>): Boolean {
        return try {
            val batch = db.batch()
            for (codeObj in codes) {
                val docRef = db.collection("generated_codes").document()
                batch.set(docRef, codeObj)
            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving codes: ", e)
            false
        }
    }

    suspend fun validateCode(inputCode: String): Boolean {
        return try {
            // Find the code document that matches and is NOT yet activated (status == false)
            val querySnapshot = db.collection("generated_codes")
                .whereEqualTo("code", inputCode)
                .whereEqualTo("status", false)
                .get()
                .await()
            
            if (!querySnapshot.isEmpty) {
                // Code is valid! Now ACTIVATE it (set status to true)
                val documentId = querySnapshot.documents[0].id
                db.collection("generated_codes").document(documentId)
                    .update("status", true)
                    .await()
                true
            } else {
                false // Code already used or doesn't exist
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error validating code: ", e)
            false
        }
    }

    suspend fun getAllCodes(): List<Code> {
        return try {
            val snapshot = db.collection("generated_codes").get().await()
            snapshot.toObjects(Code::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting codes: ", e)
            emptyList()
        }
    }
}
