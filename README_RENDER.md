Deploying this Spring Boot app to Render

Overview
- This repository contains a Spring Boot app (Maven). The `render.yaml` file defines a Web Service for Render to build and run.
- `src/main/resources/application.properties` reads the port from the `PORT` environment variable (Render sets this automatically).

Steps to deploy
1. Push your project to a Git provider supported by Render (GitHub is typical):
   - git add .
   - git commit -m "Prepare for Render deployment"
   - git push origin main

2. Create a new Web Service on Render
   - Log in to https://render.com and click "New" → "Web Service".
   - Connect your GitHub/Git provider and select the repository.
   - Render will detect `render.yaml` and use it to create the service automatically (Build and Start commands are taken from the file).
   - If you prefer manual setup, use these values:
     - Environment: Java
     - Build Command: mvn -DskipTests package
     - Start Command: java -jar target/math-assistant-0.0.1-SNAPSHOT.jar
     - Instance Type: Free / Starter (choose as appropriate)
     - Auto Deploy: Enabled (optional)

3. Environment and port
- The app uses `server.port=${PORT:8090}` so Render's `$PORT` will be picked up automatically. No extra config is required.

Docker deployment (this repo)
- This repo now includes a `Dockerfile` and `render.yaml` configured to deploy the service using Render's Docker environment.
- The provided `Dockerfile` is multi-stage: it builds the jar using Maven and then runs it on a small JRE image. Render will build the Docker image and run the container.

Notes if you want to deploy using Docker on Render:
1. Push the repo to your Git provider:
  - git add .
  - git commit -m "Add Dockerfile and Render manifest"
  - git push origin main

2. On Render create a new service:
  - New → Web Service
  - Connect the repository
  - Render will detect `render.yaml` and create a Docker-based Web Service.

3. If Render fails to build the image because of a different jar name, check the build logs for the produced artifact name and update the `Dockerfile` COPY command or the `startCommand` accordingly.

4. Verify deployment
- After deploy finishes, open the service URL Render provides (e.g., https://math-assistant.onrender.com).
- Navigate to the Fibonacci section, enter a value (e.g., 10) and submit. The SVG should show on the page.

Troubleshooting
- If the app fails to start, check Render's build and start logs.
- If you get HTTP 502/503: ensure the `startCommand` matches the built jar filename. If your artifactId/version differs, update `render.yaml` startCommand accordingly.
- If the Fibonacci plot doesn't appear:
  - Check server logs for exception stack traces (they will appear in Render's logs).
  - Ensure the controller sets the `svg` model attribute and that `th:utext` is used in the template (already present).

Advanced
- Consider adding a `Procfile` or a `Dockerfile` if you need more control over the runtime environment.
- For production builds, enable tests and disable `-DskipTests` in `render.yaml` if desired.
