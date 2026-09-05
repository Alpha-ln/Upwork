# Implementation Plan - Change App Themes to Icon Colors

Update the application's color scheme from green to the red and blue colors found in the Java logo icon.

## Proposed Changes

### [Component Name]

#### [MODIFY] [colors.xml](file:///C:/Users/xafro/AndroidStudioProjects/Upwork/app/src/main/res/values/colors.xml)
- Define `brand_red` (#EA2D2E) and `brand_blue` (#0073B7).

#### [MODIFY] [themes.xml](file:///C:/Users/xafro/AndroidStudioProjects/Upwork/app/src/main/res/values/themes.xml)
- Update `colorPrimary` to `@color/brand_red`.
- Update `colorSecondary` to `@color/brand_blue`.

#### [MODIFY] [themes.xml (night)](file:///C:/Users/xafro/AndroidStudioProjects/Upwork/app/src/main/res/values-night/themes.xml)
- Update `colorPrimary` to `@color/brand_red`.
- Update `colorSecondary` to `@color/brand_blue`.

#### [MODIFY] [login_layout.xml](file:///C:/Users/xafro/AndroidStudioProjects/Upwork/app/src/main/res/layout/login_layout.xml)
- Replace `@color/green` with `@color/brand_red` for main backgrounds, box strokes, and button tints.
- Replace `@color/green` text colors with `@color/brand_red`.

#### [MODIFY] [sign_up_layout.xml](file:///C:/Users/xafro/AndroidStudioProjects/Upwork/app/src/main/res/layout/sign_up_layout.xml)
- Replace `@color/green` with `@color/brand_red`.
- Replace `@color/green_2` with `@color/brand_blue` for accent text and shadows.

#### [MODIFY] [bg_category_tag.xml](file:///C:/Users/xafro/AndroidStudioProjects/Upwork/app/src/main/res/drawable/bg_category_tag.xml)
- Update to use `@color/brand_red`.

## Verification Plan

### Manual Verification
- Deploy the app and verify the new color scheme on `LoginActivity` and `SignUpActivity`.
- Check both Light and Dark modes.
