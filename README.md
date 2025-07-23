# AppendixCreator

**AppendixCreator** is a desktop application for generating detailed monthly invoice appendices for maintenance or construction work. Built with **Kotlin**, **Jetpack Compose for Desktop**, and **Exposed ORM**, it allows for intuitive management of buildings, jobs, materials, and monthly reporting.

---

## ✨ Features

- 🏢 Add and manage buildings (addresses)
- 🧱 Log work jobs with:
  - Date of execution
  - Labor hours
  - Travel cost (optional)
  - VAT rate (8% or 23%)
- 📦 Assign materials to jobs using a shared catalog
- 📅 Group jobs by month and building
- 🧾 Export monthly reports for invoicing

---

## 🛠️ Tech Stack

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose for Desktop](https://www.jetbrains.com/lp/compose/)
- [Exposed ORM (DAO)](https://github.com/JetBrains/Exposed)
- [PostgreSQL](https://www.postgresql.org/)
- Gradle Kotlin DSL

---

## 🚀 Getting Started

### 📦 Requirements

- JDK 17+
- PostgreSQL installed and running
- IntelliJ IDEA (recommended)
- Git

### 🔧 Setup Instructions

1. **Clone the repository:**

   ```bash
   git clone https://github.com/Serafin06/appendixCreator.git
   cd appendixCreator
   ```

2. **Configure the database:**

   Create a PostgreSQL database, e.g. `appendix_creator`.

3. **Add your DB credentials:**

   Create a file at `src/main/resources/db.properties` with the following:

   ```properties
   db.url=jdbc:postgresql://localhost:5432/appendix_creator
   db.user=your_db_user
   db.password=your_db_password
   ```

4. **Run the app:**

   ```bash
   ./gradlew run
   ```

   Or from IntelliJ: right-click `Main.kt` → Run.

---

## 📁 Project Structure

```
appendixCreator/
├── src/
│   └── main/
│       ├── kotlin/
│       │   ├── data/         # Exposed ORM models and tables
│       │   ├── ui/           # Compose UI views
│       │   └── Main.kt       # App entry point
│       └── resources/
│           └── db.properties # Database configuration
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🔜 Roadmap

- [ ] Export PDF appendices per building/month
- [ ] Add/edit/remove job entries from UI
- [ ] Basic dashboard with monthly overview
- [ ] History view for previous reports
- [ ] Optional authentication layer
- [ ] Support for SQLite (offline mode)

---

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a new branch (`git checkout -b feature-name`)
3. Commit your changes
4. Push to your fork and open a Pull Request

---

## 👤 Author

Made with ❤️ by **Serafin06**
✉️ Feel free to connect or contribute!

---

## 📄 License

Licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
