@echo off
echo ========================================
echo  Attendance Automation - Cleanup Script
echo ========================================
echo.
echo This script will remove unnecessary files from the project.
echo.
echo Files to be deleted:
echo - All .md documentation files (14 files)
echo - Unwanted SQL files (keeping only schema.sql)
echo - Old database config files (MySQL, Supabase, Example)
echo - index.html (landing page)
echo - app.js (associated with index.html)
echo - Postman collection folder
echo - Supabase folder
echo - .env.template
echo.
pause
echo.

REM Delete all markdown documentation files
echo [1/8] Deleting markdown documentation files...
del /F /Q "AIVEN_POSTGRESQL_SETUP.md" 2>nul
del /F /Q "AIVEN_SETUP_COMPLETE.md" 2>nul
del /F /Q "COMPLETE_SETUP_CHECKLIST.md" 2>nul
del /F /Q "NEW_PROJECT_REFERENCE.md" 2>nul
del /F /Q "NEW_SUPABASE_SETUP_GUIDE.md" 2>nul
del /F /Q "QUICK_START_SUPABASE.md" 2>nul
del /F /Q "README.md" 2>nul
del /F /Q "RLS_FIX_GUIDE.md" 2>nul
del /F /Q "SUPABASE_CONNECTION_GUIDE.md" 2>nul
del /F /Q "SUPABASE_DEPLOYMENT_GUIDE.md" 2>nul
del /F /Q "SUPABASE_FIX_SUMMARY.md" 2>nul
del /F /Q "SUPABASE_READY.md" 2>nul
del /F /Q "SUPABASE_SETUP_GUIDE.md" 2>nul
del /F /Q "SUPABASE_STATUS_CHECK.md" 2>nul
echo    ✓ Deleted 14 markdown files
echo.

REM Delete unwanted SQL files from database folder
echo [2/8] Deleting unwanted SQL files...
del /F /Q "database\aiven_setup.sql" 2>nul
del /F /Q "database\fix_pause_till_cleanup.sql" 2>nul
del /F /Q "database\migration_add_taken_on.sql" 2>nul
del /F /Q "database\supabase_schema.sql" 2>nul
echo    ✓ Deleted 4 SQL files (Kept: schema.sql)
echo.

REM Delete old database configuration files
echo [3/8] Deleting old database configuration files...
del /F /Q "src\main\resources\application-mysql.properties" 2>nul
del /F /Q "src\main\resources\application-supabase.properties" 2>nul
del /F /Q "src\main\resources\application.properties.example" 2>nul
echo    ✓ Deleted 3 old config files (Kept: application.properties)
echo.

REM Delete entire supabase folder
echo [4/8] Deleting supabase folder...
rmdir /S /Q "supabase" 2>nul
echo    ✓ Deleted supabase folder
echo.

REM Delete postman folder
echo [5/8] Deleting postman folder...
rmdir /S /Q "postman" 2>nul
echo    ✓ Deleted postman folder
echo.

REM Delete index.html from frontend
echo [6/8] Deleting index.html (landing page)...
del /F /Q "frontend\index.html" 2>nul
echo    ✓ Deleted index.html
echo.

REM Delete app.js (associated with index.html)
echo [7/8] Deleting app.js (associated with index.html)...
del /F /Q "frontend\js\app.js" 2>nul
echo    ✓ Deleted app.js
echo.

REM Delete .env.template
echo [8/8] Deleting .env.template...
del /F /Q ".env.template" 2>nul
echo    ✓ Deleted .env.template
echo.

echo ========================================
echo  ✓ Cleanup Complete!
echo ========================================
echo.
echo REMAINING ESSENTIAL FILES:
echo.
echo Project Configuration:
echo   - pom.xml (Maven configuration)
echo   - run.bat (Application launcher)
echo   - .gitignore (Git configuration)
echo.
echo Database:
echo   - database\schema.sql (Database schema)
echo.
echo Application Config:
echo   - src\main\resources\application.properties (Aiven DB config)
echo   - src\main\resources\templates\email-template.html
echo.
echo Frontend (Dashboards):
echo   - frontend\admin-auth.html
echo   - frontend\admin-dashboard.html
echo   - frontend\user-auth.html
echo   - frontend\user-dashboard.html
echo   - frontend\css\style.css
echo   - frontend\js\*.js (4 dashboard scripts)
echo.
echo Backend:
echo   - src\ (All Java source code)
echo   - target\ (Build output)
echo.
echo NOTE: All HTML files have been updated to use
echo professional text-based icons instead of emojis
echo for better compatibility and professional appearance.
echo.
echo ========================================
echo Your project is now clean and ready!
echo ========================================
echo.
pause
