# LayerTen

LayerTen is a modern content platform focused on countdown/Top-10 style ranked lists and blog posts. The platform enables content creators to publish engaging ranked lists with a reveal-style user experience, alongside traditional blog posts.

## Architecture

This is a monorepo web application with:
- **Backend**: Spring Boot 3.x REST API (Java 21) in `/server`
- **Frontend**: React + TypeScript + Vite in `/client`
- **Database**: PostgreSQL

## Prerequisites

- **Java 21** or higher
- **Node.js 18** or higher
- **Docker** and **Docker Compose** (for local PostgreSQL)

## Local Development Setup

### 1. Set Up Railway Database (Recommended)

Instead of running PostgreSQL locally, connect to a Railway non-prod environment:

1. Create a Railway project with PostgreSQL
2. Get your database credentials from Railway dashboard
3. Configure your local profile (see `RAILWAY_SETUP.md` for detailed instructions)

**Quick Start:**
```bash
cd server/src/main/resources
# Edit application-local.yml with your Railway credentials
```

### 2. Start Backend Server

```bash
cd server
./gradlew bootRun --args='--spring.profiles.active=local'
```

The backend will:
- Connect to your Railway database
- Run Flyway migrations automatically (first time only)
- Start on `http://localhost:8080`

**Environment Variables** (optional):
- `DB_HOST`: Database host (from Railway)
- `DB_PORT`: Database port (default: `5432`)
- `DB_NAME`: Database name (usually `railway`)
- `DB_USER`: Database username (usually `postgres`)
- `DB_PASSWORD`: Database password (from Railway)
- `ADMIN_USERNAME`: Admin username (default: `admin`)
- `ADMIN_PASSWORD`: Admin password (default: `admin`)
- `MEDIA_ROOT`: Media storage path (default: `./local-media`)

### 3. Start Frontend Development Server

Navigate to the client directory:

```bash
cd client
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend will start on `http://localhost:3000` and proxy API requests to the backend.

## Building for Production

### Backend

```bash
cd server
./gradlew build
```

The JAR file will be in `server/build/libs/`.

### Frontend

```bash
cd client
npm run build
```

The static files will be in `client/dist/`.

## Running Tests

### Backend Tests

```bash
cd server
./gradlew test
```

### Frontend Tests

```bash
cd client
npm test
```

## Project Structure

```
layerten/
├── server/                 # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/      # Java source code
│   │   │   └── resources/ # Application config & migrations
│   │   └── test/          # Backend tests
│   ├── build.gradle       # Gradle dependencies
│   └── gradlew           # Gradle wrapper
├── client/                # React frontend
│   ├── src/
│   │   ├── components/   # React components
│   │   ├── pages/        # Page components
│   │   ├── services/     # API client
│   │   └── main.tsx      # Entry point
│   ├── package.json      # NPM dependencies
│   └── vite.config.ts    # Vite configuration
├── docker-compose.yml    # Local PostgreSQL setup
└── README.md            # This file
```

## Technology Stack

### Backend
- **Spring Boot 3.2.0** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database access
- **Flyway** - Database migrations
- **PostgreSQL** - Database
- **Gradle** - Build tool
- **JUnit QuickCheck** - Property-based testing

### Frontend
- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool & dev server
- **React Router** - Client-side routing
- **TailwindCSS** - Styling
- **Axios** - HTTP client
- **fast-check** - Property-based testing

## Deployment

### Railway Deployment

The application is designed to deploy to Railway with minimal configuration:

1. **Backend Service**:
   - Add PostgreSQL plugin (provides `DATABASE_URL`)
   - Add Volume mounted at `/mnt/media`
   - Set environment variables:
     - `ADMIN_USERNAME`
     - `ADMIN_PASSWORD`
     - `MEDIA_ROOT=/mnt/media`

2. **Frontend Service** (optional separate deployment):
   - Build command: `npm run build`
   - Start command: Serve static files from `dist/`

See deployment documentation for detailed Railway setup instructions.

## API Documentation

The backend exposes RESTful endpoints:

### Public Endpoints
- `GET /api/lists` - Get paginated ranked lists
- `GET /api/lists/{slug}` - Get list detail with entries
- `GET /api/posts` - Get paginated blog posts
- `GET /api/posts/{slug}` - Get post detail
- `POST /api/suggestions` - Submit a suggestion
- `GET /api/media/{id}` - Get media file

### Admin Endpoints (require authentication)
- `POST /api/admin/lists` - Create ranked list
- `PUT /api/admin/lists/{id}` - Update ranked list
- `DELETE /api/admin/lists/{id}` - Delete ranked list
- `POST /api/admin/posts` - Create blog post
- `PUT /api/admin/posts/{id}` - Update blog post
- `DELETE /api/admin/posts/{id}` - Delete blog post
- `POST /api/admin/media` - Upload media
- `GET /api/admin/suggestions` - Get all suggestions
- `PUT /api/admin/suggestions/{id}` - Update suggestion status

## License

This project is proprietary software.
