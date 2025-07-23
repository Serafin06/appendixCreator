# AppendixCreator

**AppendixCreator** is a desktop application for generating detailed monthly invoice appendices for maintenance or construction work. Built with Kotlin, Jetpack Compose for Desktop, and Exposed ORM, it allows for easy management of buildings, jobs, materials, and invoice-related reporting.

## ✨ Features

- 📋 Create and manage buildings (addresses)
- 🧱 Log jobs with materials, labor hours, VAT rates, and optional travel cost
- 📦 Material selection based on a price catalog
- 🧾 Generate monthly reports per building as invoice appendices
- 📆 Group jobs by execution date
- 💾 Connects to PostgreSQL using Exposed ORM (DAO)

## 🛠️ Technologies Used

- **Kotlin**
- **Jetpack Compose for Desktop**
- **Exposed ORM (DAO)**
- **PostgreSQL**
- **Gradle**

## 🚀 Getting Started

### Prerequisites

- JDK 17 or newer
- PostgreSQL installed and running
- IntelliJ IDEA (recommended)

### Database Setup

1. Create a PostgreSQL database named `appendix_creator`.

2. Add a `db.properties` file inside `src/main/resources/` with the following content:

   ```properties
   db.url=jdbc:postgresql://localhost:5432/appendix_creator
   db.user=your_username
   db.password=your_password
The application will auto-create all required tables on the first launch.

Running the App
Use Gradle to run the application:

bash
Kopiuj
Edytuj
./gradlew run
Or run Main.kt from your IDE.
--
## 📁 Project Structure
bash
Kopiuj
Edytuj
appendixCreator/
├── src/
│   └── main/
│       ├── kotlin/
│       │   ├── data/             # Exposed entities and tables
│       │   ├── ui/               # Compose UI screens
│       │   └── Main.kt           # App entry point
│       └── resources/
│           └── db.properties     # Database config
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
--
## 🧪 Sample Data
You can prepopulate materials and buildings manually or by adding seed logic during initialization. PostgreSQL is used for persistent storage.

## 🔜 Planned Features
Export invoice appendices to PDF

- Editable job entries

- History of reports per month

- Authentication (optional)

- Extended material and VAT management

- Optional SQLite support for offline/local usage
---
## 🤝 Contributing
Contributions are welcome! If you'd like to contribute:

Fork the repository

Create a new branch

Commit your changes

Open a Pull Request
--
## 📜 License
This project is licensed under the MIT License. See the LICENSE file for details.
