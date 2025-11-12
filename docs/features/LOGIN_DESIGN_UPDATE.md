# Login Design Update - Yellow & Blue Theme

## Overview
Updated the login page design to match the reference image with a modern glassmorphic style using yellow and blue color scheme.

## Changes Made

### Visual Design
1. **Background**: Changed to gradient blue background (linear gradient from #1e3c72 to #2a5298)
2. **Login Card**: Implemented glassmorphic effect with semi-transparent white background and rounded corners (20px radius)
3. **Color Scheme**: Applied yellow and blue theme throughout
   - Primary Blue: #1e3c72, #2a5298
   - Accent Yellow: #f5c542, #d4a229

### Layout Components

#### Header Section
- **Logo Label**: "Your logo" text at the top
- **Login Title**: Large "Login" heading

#### Form Fields
- **Email Field**: Styled input with placeholder "username@gmail.com"
- **Password Field**: Styled input with placeholder "Password"
- Both fields have rounded corners (10px) and proper spacing

#### Buttons
- **Sign In Button**: Full-width yellow gradient button (#f5c542 to #d4a229)
- **Register Button**: Text link style button for "Register for free"
- **Theme Toggle**: Circular button with sun/moon icon for light/dark mode

#### Additional Elements
- **Forgot Password**: Link styled in blue
- **Social Login Section**: 
  - "or continue with" separator
  - Visual placeholder buttons for Google (G), GitHub (⚙), Facebook (f)
  - Note: These are visual elements only, no functionality added per requirements
- **Sign Up Prompt**: "Don't have an account yet? Register for free"

### Theme Support
- Fully supports both light and dark modes
- Dark mode uses darker blue gradients and semi-transparent backgrounds
- Automatic theme switching updates all colors dynamically

### Key Features Maintained
- Email/username input validation
- Password field security
- Enter key support for login
- Existing signup navigation
- Theme toggle functionality

## Files Modified
- `src/gui/views/LoginView.java` - Complete redesign of login interface

## Color Palette

### Light Mode
- Background: Linear gradient (#1e3c72 → #2a5298 → #1e3c72)
- Card: rgba(255, 255, 255, 0.92)
- Primary Text: #1e3c72
- Secondary Text: #555555, #666666
- Button: Linear gradient (#f5c542 → #d4a229)
- Links: #2a5298

### Dark Mode
- Background: Linear gradient (#1a2a6c → #0d1b4d → #1a2a6c)
- Card: rgba(30, 40, 70, 0.85)
- Primary Text: #ffffff
- Secondary Text: #e0e0e0, #b0b0b0
- Button: Linear gradient (#f5c542 → #d4a229)
- Links: #6fb1fc

## Notes
- Social login buttons are visual placeholders as per requirements
- No new functional buttons were added
- Existing login and signup functionality preserved
- Design is responsive and maintains proper spacing
