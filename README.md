# Appendix Creator

A desktop application for managing construction work, buildings, and materials with automatic invoice appendix generation.

## 🏗️ Features

- **Building Management** - Track multiple construction sites
- **Work Logging** - Record daily work with hours, costs, and VAT
- **Material Tracking** - Manage materials used in each project
- **Invoice Appendix Generation** - Automatically create detailed work reports (PDF)
- **Monthly Overview** - Quick access to work history by month

## 🛠️ Tech Stack

- **Kotlin** - Primary programming language
- **Compose Desktop** - Modern UI framework
- **Hibernate 6** - ORM for database operations
- **PostgreSQL** - Database (Supabase)
- **Gradle** - Build automation
- **Clean Architecture** - SOLID principles + repository pattern

## 📁 Project Structure
```
src/main/kotlin/
├── config/             # Database configuration
├── domain/             # Business entities (framework-agnostic)
│   ├── model/          # Domain models
│   └── repository/     # Repository interfaces
├── data/               # Data layer
│   ├── entity/         # JPA entities
│   └── repository/     # Repository implementations
├── application/        # Business logic
│   └── usecase/        # Use cases (Single Responsibility)
└── presentation/       # UI layer
    └── compose/        # Compose Desktop screens
```

## 🚀 Getting Started

### Prerequisites

- **JDK 17** or higher
- **PostgreSQL** database (or Supabase account)
- **Gradle** (included via wrapper)

### Installation

1. **Clone the repository**
```bash
   git clone https://github.com/Serafin06/appendixCreator.git
   cd appendixCreator
```

2. **Configure environment variables**

   Create a `.env` file in the project root:
```env
   DB_HOST=your-database-host.supabase.co
   DB_PORT=5432
   DB_NAME=postgres
   DB_USER=postgres
   DB_PASSWORD=your_secure_password
```

3. **Create database tables**

   Run the SQL script in your PostgreSQL database (see [Database Setup](#database-setup))

4. **Build the project**
```bash
   ./gradlew build
```

5. **Run the application**
```bash
   ./gradlew run
```

## 🗄️ Database Setup

Execute this SQL in your PostgreSQL database:
```sql
-- Buildings table
CREATE TABLE budynki (
    id BIGSERIAL PRIMARY KEY,
    adres VARCHAR(500) NOT NULL
);

-- Materials table
CREATE TABLE materialy (
    id BIGSERIAL PRIMARY KEY,
    nazwa VARCHAR(255) NOT NULL,
    jednostka VARCHAR(50) NOT NULL,
    cena_za_jednostke DECIMAL(10, 2) NOT NULL CHECK (cena_za_jednostke >= 0)
);

-- Work table
CREATE TABLE praca (
    id BIGSERIAL PRIMARY KEY,
    data DATE NOT NULL,
    opis TEXT NOT NULL,
    roboczogodziny INTEGER NOT NULL CHECK (roboczogodziny BETWEEN 1 AND 30),
    koszt_dojazdu DECIMAL(10, 2) NOT NULL CHECK (koszt_dojazdu >= 0),
    vat INTEGER NOT NULL CHECK (vat IN (8, 23)),
    budynek_id BIGINT NOT NULL,
    FOREIGN KEY (budynek_id) REFERENCES budynki(id) ON DELETE CASCADE
);

-- Work-Material junction table
CREATE TABLE praca_material (
    id BIGSERIAL PRIMARY KEY,
    ilosc DECIMAL(10, 3) NOT NULL CHECK (ilosc > 0),
    praca_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    FOREIGN KEY (praca_id) REFERENCES praca(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES materialy(id) ON DELETE CASCADE
);

-- Performance indexes
CREATE INDEX idx_praca_budynek ON praca(budynek_id);
CREATE INDEX idx_praca_data ON praca(data);
CREATE INDEX idx_praca_material_praca ON praca_material(praca_id);
CREATE INDEX idx_praca_material_material ON praca_material(material_id);
```

## 🔐 Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_HOST` | Database host | `db.xxx.supabase.co` |
| `DB_PORT` | Database port | `5432` |
| `DB_NAME` | Database name | `postgres` |
| `DB_USER` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `your_password` |

**⚠️ Security Note:** Never commit `.env` file to version control!

## 🏛️ Architecture

This project follows **Clean Architecture** principles:

### Layers

1. **Domain Layer** - Pure business logic, no framework dependencies
    - Domain models (POJOs)
    - Repository interfaces
    - Business rules

2. **Data Layer** - Database access
    - JPA entities
    - Repository implementations
    - Hibernate configuration

3. **Application Layer** - Use cases
    - Single-purpose business operations
    - Orchestrates domain objects
    - Independent of UI

4. **Presentation Layer** - User interface
    - Compose Desktop components
    - ViewModels (state management)
    - Screen navigation

### Design Patterns

- **Repository Pattern** - Abstract data access
- **Use Case Pattern** - Encapsulate business logic
- **Dependency Injection** - Loose coupling (manual)
- **Entity Mapper** - Separate domain from persistence

## 🧪 Testing
```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport
```

## 📦 Building Distributable
```bash
# Create platform-specific installer
./gradlew packageDistributionForCurrentOS

# Outputs to: build/compose/binaries/main/
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use SOLID principles
- Write meaningful commit messages
- Add tests for new features

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**RafApp**

- GitHub: [@Serafin06](https://github.com/Serafin06)

## 🙏 Acknowledgments

- [Compose Desktop](https://www.jetbrains.com/lp/compose-desktop/) - UI Framework
- [Hibernate](https://hibernate.org/) - ORM
- [Supabase](https://supabase.com/) - Database hosting
- [Kotlin](https://kotlinlang.org/) - Programming language

## 📚 Documentation

For detailed documentation, see:
- [API Documentation](docs/API.md)
- [Database Schema](docs/DATABASE.md)
- [User Guide](docs/USER_GUIDE.md)

## 🐛 Known Issues

- None currently reported

## 🗺️ Roadmap

- [ ] PDF invoice generation
- [ ] Export to Excel
- [ ] Multi-language support (Polish/English)
- [ ] Mobile version (Flutter)
- [ ] Cloud synchronization
- [ ] Advanced reporting and analytics

## 📞 Support

If you have any questions or issues, please open an issue on GitHub.

---

**Made with ❤️ using Kotlin and Compose Desktop**
```

---

## 📄 LICENSE (MIT)
```
MIT License

Copyright (c) 2025 RafApp

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.