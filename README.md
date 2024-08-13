
Download the demo app from the below drive link

https://drive.google.com/file/d/1JbJCWWy28FUm80cjBbqjUy5HfnvpzK2o/view?usp=drivesdk

# Tale App with Vertex AI for Firebase Integration

This Android app is designed to communicate with the Vertex AI for Firebase SDK, leveraging the Gemini generative model capabilities. This README provides instructions on how to set up and configure the project to run successfully.

## Prerequisites

To work on this codebase, you need to have the following:

1. **Firebase Project Setup**
   - Create a Firebase project for the app.
   - Enable Vertex AI API on the Google Cloud Console account connected to the Firebase project.

2. **Configuration**
   - Download the `google-services.json` file from your Firebase project.
   - Place the `google-services.json` file in the `app` directory of the project.

3. **Firebase Authentication Setup**
   - Enable Firebase Authentication in your Firebase project.
   - Enable the **Anonymous Sign-In** method, as it is used for authentication in this app.

## Setup Instructions

1. Clone this repository:
   ```bash
   git clone https://github.com/africantechdotworld/tale-gemini.git
   cd your-repo
   ```

2. Place the `google-services.json` file under the `app/` directory.

3. Make sure Firebase Authentication and Vertex AI are properly configured in your Firebase project.

4. Build and run the app on an Android device or emulator.

## Documentation

For further guidance and information, refer to the following official documentation:

- [Firebase Android App Integration](https://firebase.google.com/docs/android/setup)
- [Vertex AI for Firebase SDK Documentation](https://firebase.google.com/docs/vertex-ai)

---
