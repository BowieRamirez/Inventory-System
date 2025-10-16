STI Merch System (Inventory-System)

Lightweight Java CLI application for managing merch and student accounts (school project).

Run & build (Windows PowerShell)

```powershell
# from project root
# Compile all sources to the out directory
Remove-Item -LiteralPath .\out -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path .\out | Out-Null
Get-ChildItem -Path .\src -Recurse -Filter *.java | ForEach-Object { $_.FullName } | Out-File -FilePath sources.txt -Encoding ASCII
cmd.exe /c "javac -d out -sourcepath src @sources.txt"
Remove-Item sources.txt -ErrorAction SilentlyContinue

# Run the CLI
java -cp out main.MerchSystem
```

Notes
- `src/database/Unifdatabase.sql` contains a SQLite-compatible schema matching the Java model classes.
- `src/database/seeds/seed_items.sql` contains INSERT statements to seed the Item table (previewed via `preview_db.py`).
- `src/database/data/users.txt` is a simple user persistence file; new signups are appended by `src/utils/FileStorage.java`.
- Compiled output `out/` is ignored by `.gitignore`.

If you want me to push to your remote repository, ensure the project has a remote configured and that credentials are available in your environment (or provide a remote URL), and I'll push the changes.
