// pm2 process file for the RFID asset tracking backend.
// Rename the app name / paths to match your actual domain and deploy directory,
// then run: pm2 start ecosystem.config.js
module.exports = {
  apps: [
    {
      name: "backend.rfid-app", // rename to match your subdomain, e.g. "backend.yourdomain.com"
      script: "java",
      interpreter: "none", // tells pm2 this isn't a Node script
      args:
        "-Xms128m -Xmx384m " +
        "-jar target/rfid-asset-tracking-backend.jar " +
        "--server.port=8089",
      cwd: "/home/rfid-inventory-management", // matches this server's actual clone path
      exec_mode: "fork",
      instances: 1,
      autorestart: true,
      watch: false,
      max_memory_restart: "450M",
      out_file: "./logs/out.log",
      error_file: "./logs/error.log",
      time: true
    }
  ]
};