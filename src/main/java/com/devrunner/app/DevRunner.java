package com.devrunner.app;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.json.JSONObject;

public class DevRunner extends JFrame {
    private Map<String, String> projectPaths;
    private Map<String, JTextField> pathFields;
    private JButton typescriptBrowserButton;
    private Process tsProcess;
    private String tsUrl;
   
    public DevRunner() {
        super("Game Engine Development Runner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
       
        // Center the window
        setLocationRelativeTo(null);
       
        // Initialize maps
        projectPaths = new HashMap<>();
        pathFields = new HashMap<>();
       
        // Load configuration
        loadConfig();
       
        // Set up the GUI
        setupUI();
       
        // Make the window visible
        setVisible(true);
    }
   
    private void loadConfig() {
        // Default paths relative to jarDevRunner directory
        projectPaths.put("java", "../Java");
        projectPaths.put("cpp", "../starterCPP");
        projectPaths.put("python", "../Python");
        projectPaths.put("typescript", "../starterTS");
       
        // Try to load from config file
        Path configPath = Paths.get("project_paths.json");
        if (Files.exists(configPath)) {
            try {
                String content = Files.readString(configPath);
                JSONObject json = new JSONObject(content);
                for (String key : json.keySet()) {
                    projectPaths.put(key, json.getString(key));
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error loading config: " + e.getMessage(),
                    "Configuration Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
   
    private void saveConfig() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, JTextField> entry : pathFields.entrySet()) {
            json.put(entry.getKey(), entry.getValue().getText());
            projectPaths.put(entry.getKey(), entry.getValue().getText());
        }
       
        try {
            Files.writeString(Paths.get("project_paths.json"), json.toString(4));
            JOptionPane.showMessageDialog(this,
                "Configuration saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error saving config: " + e.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
   
    private void setupUI() {
        // Set up the main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
       
        // OS Information at the top
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
       
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        boolean isWindows = osName.toLowerCase().contains("windows");
       
        JLabel osLabel = new JLabel("Operating System: " + osName + " (" + osArch + ")");
        osLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        mainPanel.add(osLabel, gbc);
       
        // Add script info for the current OS
        gbc.gridy = 1;
        JLabel scriptLabel = new JLabel("Script type: " + (isWindows ? "PowerShell (.ps1)" : "Shell (.sh)"));
        scriptLabel.setFont(new Font("Dialog", Font.ITALIC, 12));
        mainPanel.add(scriptLabel, gbc);
       
        // Update the grid position for the next components
        gbc.gridy = 2;
       
        // Create left and right panels
        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();
       
        // Add panels to main panel
        // gbc.gridy already set to 2 above
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
       
        mainPanel.add(leftPanel, gbc);
       
        gbc.gridx = 1;
        mainPanel.add(rightPanel, gbc);
       
        // Add main panel to frame
        add(mainPanel);
    }
   
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Project Paths Configuration"));
       
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
       
        // Create a fixed width for the icon column
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
       
        int row = 0;
        for (String project : new String[]{"java", "cpp", "python", "typescript"}) {
            // Icon
            gbc.gridy = row;
            gbc.gridx = 0;
            ImageIcon icon = loadIcon("/icons/" + project + ".png");
            if (icon != null) {
                JLabel iconLabel = new JLabel(icon);
                // Set a fixed size for icon container
                iconLabel.setPreferredSize(new Dimension(30, 24));
                panel.add(iconLabel, gbc);
            }
           
            // Label
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 10, 5, 5); // Add more left padding
            panel.add(new JLabel(project.substring(0, 1).toUpperCase() +
                               project.substring(1) + " Project Path:"), gbc);
           
            // Text field
            gbc.gridx = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            JTextField field = new JTextField(projectPaths.get(project));
            pathFields.put(project, field);
            panel.add(field, gbc);
           
            row++;
        }
       
        // Save button
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton saveButton = new JButton("Save Paths");
        saveButton.addActionListener(e -> saveConfig());
        panel.add(saveButton, gbc);
       
        return panel;
    }
   
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Launch Projects"));
       
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
       
        // Create a fixed width for the icon column
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
       
        // Add project buttons with aligned icons
        int row = 0;
        for (String project : new String[]{"java", "cpp", "python"}) {
            addAlignedProjectButton(panel, gbc, row++, project,
                "Run " + project.substring(0, 1).toUpperCase() +
                project.substring(1) + " Project");
        }
       
        // TypeScript section with two buttons
        gbc.gridy = row;
        gbc.gridx = 0;
        ImageIcon tsIcon = loadIcon("/icons/typescript.png");
        if (tsIcon != null) {
            JLabel iconLabel = new JLabel(tsIcon);
            iconLabel.setPreferredSize(new Dimension(30, 24));
            panel.add(iconLabel, gbc);
        }
       
        // TypeScript buttons panel
        JPanel tsButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton tsButton = new JButton("Run TypeScript Project");
        tsButton.addActionListener(e -> runProject("typescript"));
        tsButtonPanel.add(tsButton);
       
        typescriptBrowserButton = new JButton("Open in Browser");
        typescriptBrowserButton.setEnabled(false);
        typescriptBrowserButton.addActionListener(e -> openTypeScriptInBrowser());
        tsButtonPanel.add(typescriptBrowserButton);
       
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(tsButtonPanel, gbc);
       
        return panel;
    }
   
    private void addAlignedProjectButton(JPanel panel, GridBagConstraints gbc,
                                       int row, String project, String buttonText) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
       
        // Add icon with fixed size
        ImageIcon icon = loadIcon("/icons/" + project + ".png");
        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setPreferredSize(new Dimension(30, 24));
            panel.add(iconLabel, gbc);
        }
       
        // Add button
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JButton button = new JButton(buttonText);
        button.addActionListener(e -> runProject(project));
        panel.add(button, gbc);
    }
   
    private ImageIcon loadIcon(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                byte[] imageBytes = is.readAllBytes();
                return new ImageIcon(new ImageIcon(imageBytes)
                    .getImage()
                    .getScaledInstance(24, 24, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
   
    private void runTypeScriptProject(Path projectPath, Map<String, String> env) throws IOException, InterruptedException {
        // First verify npm is available and get its path
        ProcessBuilder npmCheck = new ProcessBuilder("which", "npm");
        npmCheck.environment().putAll(env);
        Process npmProcess = npmCheck.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(npmProcess.getInputStream()))) {
            String npmPath = reader.readLine();
            if (npmPath == null || npmPath.isEmpty()) {
                throw new IOException("npm not found. Please install Node.js from https://nodejs.org/");
            }
           
            // Now we know npm exists, let's use it
            if (!Files.exists(projectPath.resolve("node_modules"))) {
                ProcessBuilder npmInstall = new ProcessBuilder(npmPath, "install");
                npmInstall.directory(projectPath.toFile());
                npmInstall.redirectErrorStream(true);
                npmInstall.environment().putAll(env);
                Process installProcess = npmInstall.start();
                startOutputMonitor(installProcess, "npm install");
                installProcess.waitFor(5, TimeUnit.MINUTES);
            }
           
            // Run the dev server
            ProcessBuilder pb = new ProcessBuilder(npmPath, "run", "dev");
            pb.directory(projectPath.toFile());
            pb.redirectErrorStream(true);
            pb.environment().putAll(env);
           
            if (tsProcess != null) {
                tsProcess.destroy();
            }
           
            typescriptBrowserButton.setEnabled(false);
            tsUrl = null;
           
            Process process = pb.start();
            tsProcess = process;
            startOutputMonitor(process, "typescript");
        }
    }

    private void runScript(ProcessBuilder pb, String scriptPath, Map<String, String> env) throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
       
        if (!isWindows) {
            // Make script executable on Unix systems
            ProcessBuilder chmodPb = new ProcessBuilder("chmod", "+x", scriptPath);
            chmodPb.directory(new File(scriptPath).getParentFile());
            chmodPb.environment().putAll(env);
            Process chmodProcess = chmodPb.start();
            chmodProcess.waitFor(5, TimeUnit.SECONDS);
        } else {
            // On Windows, ensure PowerShell scripts can run
            // This is usually handled by setting ExecutionPolicy when invoking PowerShell
            if (scriptPath.toLowerCase().endsWith(".ps1")) {
                // Check if script exists
                if (!Files.exists(Paths.get(scriptPath))) {
                    throw new IOException("PowerShell script not found: " + scriptPath);
                }
            }
        }
    }

    private boolean installJDK() throws IOException, InterruptedException {
        String wingetCommand = "winget install -e --id Oracle.JDK.17";
        ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", wingetCommand);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        startOutputMonitor(process, "JDK Installation");
        return process.waitFor() == 0;
    }

    private boolean installCMake() throws IOException, InterruptedException {
        String wingetCommand = "winget install -e --id Kitware.CMake";
        ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", wingetCommand);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        startOutputMonitor(process, "CMake Installation");
        return process.waitFor() == 0;
    }

    private boolean installPython() throws IOException, InterruptedException {
        String wingetCommand = "winget install -e --id Python.Python.3.9";
        ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", wingetCommand);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        startOutputMonitor(process, "Python Installation");
        return process.waitFor() == 0;
    }

    private boolean installNodejs() throws IOException, InterruptedException {
        String wingetCommand = "winget install -e --id OpenJS.NodeJS.LTS";
        ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", wingetCommand);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        startOutputMonitor(process, "Node.js Installation");
        return process.waitFor() == 0;
    }

    private boolean installMinGW() throws IOException, InterruptedException {
        String wingetCommand = "winget install -e --id MSYS2.MSYS2";
        ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", wingetCommand);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        startOutputMonitor(process, "MSYS2 Installation");
        if (process.waitFor() == 0) {
            // Install MinGW toolchain through MSYS2
            ProcessBuilder msys2Pb = new ProcessBuilder(
                "C:\\msys64\\usr\\bin\\bash.exe",
                "-lc",
                "pacman -S --noconfirm mingw-w64-x86_64-gcc mingw-w64-x86_64-make"
            );
            msys2Pb.redirectErrorStream(true);
            Process msys2Process = msys2Pb.start();
            startOutputMonitor(msys2Process, "MinGW Installation");
            return msys2Process.waitFor() == 0;
        }
        return false;
    }

    private boolean checkDependency(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("where", command);
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void installMissingDependencies(String projectType) throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        if (!isWindows) return;

        switch (projectType) {
            case "java":
                if (!checkDependency("javac")) {
                    if (installJDK()) {
                        JOptionPane.showMessageDialog(this,
                            "Java Development Kit has been installed. Please restart the application.",
                            "Installation Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                        System.exit(0);
                    }
                }
                break;

            case "cpp":
                boolean cmakeInstalled = checkDependency("cmake");
                boolean mingwInstalled = checkDependency("g++");
               
                if (!cmakeInstalled) {
                    installCMake();
                }
                if (!mingwInstalled) {
                    installMinGW();
                }
               
                if (!cmakeInstalled || !mingwInstalled) {
                    JOptionPane.showMessageDialog(this,
                        "Development tools have been installed. Please restart the application.",
                        "Installation Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
                break;

            case "python":
                if (!checkDependency("python")) {
                    if (installPython()) {
                        JOptionPane.showMessageDialog(this,
                            "Python has been installed. Please restart the application.",
                            "Installation Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                        System.exit(0);
                    }
                }
                break;

            case "typescript":
                if (!checkDependency("npm")) {
                    if (installNodejs()) {
                        JOptionPane.showMessageDialog(this,
                            "Node.js has been installed. Please restart the application.",
                            "Installation Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                        System.exit(0);
                    }
                }
                break;
        }
    }

    private boolean isWSLAvailable() {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return false;
        }
       
        try {
            ProcessBuilder pb = new ProcessBuilder("wsl", "--status");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void runProject(String projectType) {
        String projectPath = pathFields.get(projectType).getText();
        Path path = Paths.get(projectPath).toAbsolutePath();
       
        // Verify the path exists and appropriate script is available
        if (!verifyScriptExists(projectType)) {
            return;
        }
       
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(path.toFile());
            pb.redirectErrorStream(true);
           
            // Add system paths to environment
            Map<String, String> env = pb.environment();
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
           
            if (isWindows) {
                // Windows: Set up paths for development tools
                String programFiles = System.getenv("ProgramFiles");
                String programFilesX86 = System.getenv("ProgramFiles(x86)");
                String existingPath = env.get("Path"); // Windows uses "Path" not "PATH"
               
                StringBuilder pathBuilder = new StringBuilder();
               
                // Add MSYS2 paths first (as per setup instructions)
                pathBuilder.append("C:\\msys64\\mingw64\\bin;");
                pathBuilder.append("C:\\msys64\\usr\\bin;");
               
                // Then add other development tool paths
                if (programFiles != null) {
                    pathBuilder.append(programFiles).append("\\Java\\jdk-17\\bin;");
                    pathBuilder.append(programFiles).append("\\CMake\\bin;");
                    pathBuilder.append(programFiles).append("\\nodejs;");
                }
                if (programFilesX86 != null) {
                    pathBuilder.append(programFilesX86).append("\\Java\\jdk-17\\bin;");
                }
               
                // Append existing PATH
                if (existingPath != null) {
                    pathBuilder.append(existingPath);
                }
               
                env.put("Path", pathBuilder.toString());
               
                // Set JAVA_HOME if not set
                if (env.get("JAVA_HOME") == null) {
                    String javaHome = programFiles + "\\Java\\jdk-17";
                    if (Files.exists(Paths.get(javaHome))) {
                        env.put("JAVA_HOME", javaHome);
                    }
                }
            } else {
                // Unix-based systems
                String existingPath = env.get("PATH");
                String systemPaths = "/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin";
                env.put("PATH", systemPaths + (existingPath != null ? ":" + existingPath : ""));
            }

            String scriptPath = null;
           
            if (isWindows) {
                // On Windows, we need to use PowerShell to run the scripts
                switch (projectType) {
                    case "java":
                    case "cpp":
                        scriptPath = path.resolve("run.ps1").toString();
                       
                        // Check if run.ps1 exists
                        if (!Files.exists(Paths.get(scriptPath))) {
                            Path shPath = path.resolve("run.sh");
                            if (Files.exists(shPath)) {
                                // Confirm with the user before trying to use WSL
                                int response = JOptionPane.showConfirmDialog(this,
                                    "Windows PowerShell script (run.ps1) not found, but run.sh exists.\n" +
                                    "Would you like to try using Windows Subsystem for Linux (WSL) to run it?",
                                    "Script Not Found",
                                    JOptionPane.YES_NO_OPTION);
                                   
                                if (response == JOptionPane.YES_OPTION) {
                                    // Try to use Windows Subsystem for Linux to run the shell script
                                    pb.command("wsl", "bash", "-c", "cd " + path + " && chmod +x ./run.sh && ./run.sh");
                                    scriptPath = shPath.toString();
                                } else {
                                    throw new IOException("PowerShell script not found, and user declined to use WSL.");
                                }
                            } else {
                                throw new IOException("Neither run.ps1 nor run.sh was found in " + path);
                            }
                        } else {
                            // Enable script execution and run the PowerShell script
                            pb.command(
                                "powershell",
                                "-ExecutionPolicy", "Bypass",
                                "-NoProfile",
                                "-File", scriptPath
                            );
                        }
                        break;
                       
                    case "python":
                        scriptPath = path.resolve("run.ps1").toString();
                       
                        // Check if run.ps1 exists
                        if (!Files.exists(Paths.get(scriptPath))) {
                            throw new IOException("PowerShell script not found for Python project");
                        }
                       
                        // Just directly call the run.ps1 script for Python projects
                        // This simplifies things by using the script that's known to work when run directly
                        pb.command(
                            "powershell",
                            "-ExecutionPolicy", "Bypass",
                            "-NoProfile",
                            "-File", scriptPath
                        );
                       
                        break;
                       
                    case "typescript":
                        // Always use run.ps1 directly for TypeScript on Windows
                        scriptPath = path.resolve("run.ps1").toString();
                       
                        if (!Files.exists(Paths.get(scriptPath))) {
                            throw new IOException("PowerShell script not found for TypeScript project. Please ensure run.ps1 exists in " + path);
                        }
                       
                        // Use the PowerShell script directly
                        pb.command(
                            "powershell",
                            "-ExecutionPolicy", "Bypass",
                            "-NoProfile",
                            "-File", scriptPath
                        );
                        break;
                       
                    default:
                        throw new IOException("Unknown project type: " + projectType);
                }
            } else {
                // Unix systems use shell scripts
                scriptPath = path.resolve("run.sh").toString();
                runScript(pb, scriptPath, env);
                pb.command("./run.sh");
            }

            // This check is now handled within the platform-specific section above

            // Start the process and monitor output
            Process process = pb.start();
            if (projectType.equals("typescript")) {
                tsProcess = process;
                typescriptBrowserButton.setEnabled(false);
                tsUrl = null;
            }
            startOutputMonitor(process, projectType);
           
        } catch (Exception e) {
            e.printStackTrace();
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
            String platformSpecificHelp = isWindows ?
                "\nOn Windows, make sure to have:\n" +
                "- PowerShell scripts (.ps1) or WSL for Linux shell scripts.\n" +
                "- PowerShell execution policy that allows scripts (Set-ExecutionPolicy RemoteSigned).\n" :
                "\nOn macOS/Linux, make sure run.sh has execute permissions (chmod +x run.sh).\n";
               
            showErrorDialog("Error",
                "Error running " + projectType + " project:\n" + e.getMessage() +
                platformSpecificHelp +
                "\n\nMake sure all required tools are installed:\n" +
                "- C++: " + (isWindows ? "MSYS2 with mingw-w64-x86_64-gcc, mingw-w64-x86_64-cmake" : "Compiler (gcc/clang) and CMake") + "\n" +
                "- TypeScript: Node.js and npm\n" +
                "- Python: Python 3.9 or later\n" +
                "- Java: JDK 17 or later");
        }
    }
   
    private void startOutputMonitor(Process process, String projectType) {
        StringBuilder outputBuffer = new StringBuilder();
        boolean[] hasError = {false}; // Array to allow modification in lambda
        
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip SDL touch device warning for C++ project
                    if (projectType.equals("cpp") && line.contains("SDL Error after display: Unknown touch device id")) {
                        continue;
                    }

                    // Print output to console for debugging
                    System.out.println(projectType + ": " + line);
                    outputBuffer.append(projectType).append(": ").append(line).append("\n");
                    
                    // For TypeScript, look for the local server URL with improved detection
                    if (projectType.equals("typescript")) {
                        String lowerLine = line.toLowerCase();
                        String extractedUrl = null;
                        
                        // Common patterns in Vite and other dev servers
                        if (lowerLine.contains("local:") || 
                            lowerLine.contains("localhost:") ||
                            lowerLine.contains("127.0.0.1:")) {
                            
                            // First strip ANSI escape codes from the line
                            String cleanLine = line.replaceAll("\u001B\\[[;\\d]*m", "");
                            
                            // Try different URL patterns
                            java.util.regex.Pattern[] patterns = {
                                // Standard Local: http://... pattern
                                java.util.regex.Pattern.compile("Local:\\s*(https?://[^\\s]+)"),
                                // Direct URL pattern
                                java.util.regex.Pattern.compile("(https?://(?:localhost|127\\.0\\.0\\.1)[^\\s]+)"),
                                // ➜ Local: pattern (Vite style)
                                java.util.regex.Pattern.compile("➜\\s*Local:\\s*(https?://[^\\s]+)"),
                                // VITE pattern
                                java.util.regex.Pattern.compile("VITE\\s*.*?(https?://(?:localhost|127\\.0\\.0\\.1)[^\\s]+)")
                            };
                            
                            // Try each pattern until we find a match
                            for (java.util.regex.Pattern pattern : patterns) {
                                java.util.regex.Matcher matcher = pattern.matcher(cleanLine);
                                if (matcher.find()) {
                                    extractedUrl = matcher.group(1);
                                    break;
                                }
                            }
                            
                            if (extractedUrl != null) {
                                // Clean up the URL - remove any remaining ANSI codes and trim
                                final String url = extractedUrl.replaceAll("\u001B\\[[;\\d]*m", "").trim();
                                System.out.println("Found TypeScript URL: " + url);
                                SwingUtilities.invokeLater(() -> {
                                    tsUrl = url;
                                    typescriptBrowserButton.setEnabled(true);
                                });
                            }
                        }
                    }
                   
                    // Show error messages in GUI if they indicate a problem
                    String lowerLine = line.toLowerCase();
                    if ((lowerLine.contains("error") && 
                         !lowerLine.contains("error after display") && 
                         !lowerLine.contains("sdl error after display")) || 
                        lowerLine.contains("exception") ||
                        lowerLine.contains("failed") ||
                        (lowerLine.contains("not found") && !lowerLine.contains("touch device")) ||
                        (lowerLine.contains("sdl") && lowerLine.contains("error") && 
                         !lowerLine.contains("touch device"))) {
                        if (!hasError[0]) { // Only show first error
                            hasError[0] = true;
                            final String errorMessage = outputBuffer.toString();
                            SwingUtilities.invokeLater(() ->
                                showErrorDialog(projectType + " Error", errorMessage));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (!hasError[0]) {
                    final String errorMessage = "Error reading " + projectType + " output:\n" + 
                        e.getMessage() + "\n\nFull output:\n" + outputBuffer.toString();
                    SwingUtilities.invokeLater(() ->
                        showErrorDialog("Error", errorMessage));
                }
            }
        }).start();
    }
   
    private void openTypeScriptInBrowser() {
        if (tsUrl != null) {
            try {
                Desktop.getDesktop().browse(new java.net.URI(tsUrl));
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error opening browser: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
   
    private void showErrorDialog(String title, String message) {
        // Create a text area for showing the error
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
       
        // Put it in a scroll pane in case the message is long
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
       
        // Show the dialog using JOptionPane
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.ERROR_MESSAGE));
    }
   
    private boolean verifyScriptExists(String projectType) {
        String projectPath = pathFields.get(projectType).getText();
        Path path = Paths.get(projectPath).toAbsolutePath();
       
        if (!Files.exists(path)) {
            showErrorDialog("Path Error", "Project path does not exist: " + path);
            return false;
        }
       
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        Path scriptPath;
       
        if (isWindows) {
            scriptPath = path.resolve("run.ps1");
            if (!Files.exists(scriptPath)) {
                // Check if run.sh exists as a fallback
                Path shPath = path.resolve("run.sh");
                if (Files.exists(shPath)) {
                    // Check if WSL is available before offering it as an option
                    if (isWSLAvailable()) {
                        int response = JOptionPane.showConfirmDialog(this,
                            "Windows PowerShell script (run.ps1) not found, but run.sh exists.\n" +
                            "Would you like to try using Windows Subsystem for Linux (WSL) to run it?",
                            "Script Not Found",
                            JOptionPane.YES_NO_OPTION);
                        return response == JOptionPane.YES_OPTION;
                    } else {
                        showErrorDialog("Script Not Found",
                            "The PowerShell script (run.ps1) was not found for this project.\n" +
                            "A shell script (run.sh) exists, but Windows Subsystem for Linux (WSL) is not available.\n\n" +
                            "Please make sure you have the appropriate script files for your platform, or install WSL.");
                        return false;
                    }
                } else {
                    showErrorDialog("Script Not Found",
                        "Neither run.ps1 nor run.sh was found in " + path +
                        "\n\nPlease make sure you have the appropriate script files for your platform.");
                    return false;
                }
            }
        } else {
            // Unix system
            scriptPath = path.resolve("run.sh");
            if (!Files.exists(scriptPath)) {
                showErrorDialog("Script Not Found",
                    "Script file run.sh not found in " + path +
                    "\n\nPlease make sure you have the appropriate script files for your platform.");
                return false;
            }
        }
       
        return true;
    }
   
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        // Create and show GUI
        SwingUtilities.invokeLater(() -> new DevRunner());
    }
}