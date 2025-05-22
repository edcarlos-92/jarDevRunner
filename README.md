# Game Engine Development Runner (jarDevRunner)

A simple Java-based GUI application to help manage and run multiple Game Engine Development course projects without constantly switching between terminal directories.

## What is this?

This tool provides a graphical interface to run your Game Engine Development course projects (Java, C++, Python, and TypeScript) from a single window. Instead of constantly switching directories in the terminal to run different projects, you can:

- Configure paths to all your projects once
- Run any project with a single click
- See project output directly in error dialogs
- Open TypeScript projects in browser automatically

## Prerequisites

- Java Development Kit (JDK) 17 or later
- For the projects themselves:
  - C++: MSYS2 with MinGW-w64
  - Python: Python 3.9 or later
  - TypeScript: Node.js and npm
  - Java: JDK 17 or later

## Quick Start

1. Copy `jarDevRunner.jar` to your course lesson workspace root directory (where all your project folders are) from `build/libs/jarDevRunner.jar`
2. Double-click the JAR file or run:
   ```
   java -jar jarDevRunner.jar
   ```
3. Configure project paths in the left panel (defaults should work if using standard directory structure)
4. Click "Save Paths" to save your configuration for later use.
5. Use the buttons on the right to run your projects

## Building from Source

### On macOS/Linux
1. Clone or download the source code
2. Build the JAR:
   ```bash
   cd path/to/jarDevRunner
   ./gradlew build
   ```
3. The JAR will be in `build/libs/jarDevRunner.jar`

To build and copy to parent directory in one command:
```bash
cd path/to/jarDevRunner && ./gradlew clean build && cp build/libs/jarDevRunner.jar ..
```

### On Windows
1. Clone or download the source code
2. Build the JAR:
   ```powershell
   cd path\to\jarDevRunner
   .\gradlew.bat build
   ```
3. The JAR will be in `build\libs\jarDevRunner.jar`

To build and copy to parent directory in one command:
```powershell
cd path\to\jarDevRunner; .\gradlew.bat clean build; copy build\libs\jarDevRunner.jar ..
```

## Project Structure Requirements

The tool expects your projects to be organized like this:
```
root/  (could be the lesson root, e.g., Lesson-20-BabyMovement)
  ├── Java/          # Java project
  ├── starterCPP/    # C++ project
  ├── Python/        # Python project
  ├── starterTS/     # TypeScript project
  └── jarDevRunner.jar
```

Each project must have its own run script:
- Windows: `run.ps1`
- Mac/Linux: `run.sh`

## Important Notes

1. This tool does NOT fix project errors. Please make sure your projects work manually first:
   - Java: Can run with `./gradlew run` or `.\gradlew.bat run`
   - C++: Can build with CMake and run
   - Python: Can run with `python src/main.py`
   - TypeScript: Can run with `npm run dev`

2. Check project-specific README files for proper setup instructions

3. If you get PowerShell execution policy errors on Windows, run:
   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```

4. The main application logic and entry point is located in `src/main/java/com/devrunner/app/DevRunner.java`

## Customizing

You can modify `project_paths.json` directly or use the GUI to change project paths. Default paths are:
- Java: `../Java`
- C++: `../starterCPP`
- Python: `../Python`
- TypeScript: `../starterTS`
  
This is because ideally the jarDevRunner.jar is built to the build folder of the application but when copied to the parent directory the paths need to change accordingly and after saving the new paths it creates a project_paths.json from which references will be made the next time we run the application.

## Troubleshooting

1. **Java Not Found**: Make sure JAVA_HOME is set and points to JDK 17+
2. **Projects Not Running**: Verify that run scripts exist and have execute permissions
3. **Long Paths on Windows**: Enable long paths in Windows settings if needed
4. **Missing Dependencies**: Install required tools for each project type first those that has been provided in the original run files will automatically be installed if there is any such provision.

Finally: Please feel free to re-innovate this and share with us if you think we can make it work in a better way. 

Kindly let me know if you run into any painful challenges. Thank you.