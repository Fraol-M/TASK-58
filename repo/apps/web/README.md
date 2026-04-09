# CampusFit Web Frontend

Vue 3 + TypeScript + Vite single-page application for the CampusFit system.

## Stack

- **Vue 3** with Composition API
- **TypeScript** (strict mode)
- **Vite** for build tooling
- **Vue Router** with route guards and role-based access
- **Pinia** for state management
- **Axios** for HTTP client
- **Vitest** + Vue Testing Library for tests

## Mock / Local Mode

When `VITE_MOCK_MODE=true`, the app runs with local mock data and does **not** connect to a real backend. A banner is displayed at the top of every page to indicate mock mode. By default, the app connects to the backend API at the URL configured in `VITE_API_BASE_URL`.

Mock data is isolated in `src/services/mock/` behind an adapter interface. The adapter factory (`src/services/adapters/adapter-factory.ts`) switches between real HTTP calls and mock responses based on the environment variable.

## Routes

| Path | Role | Description |
|------|------|-------------|
| `/sign-in` | Public | Sign in |
| `/sign-up` | Public | Sign up |
| `/dashboard` | All authenticated | Role-aware dashboard |
| `/notifications` | All authenticated | Notification inbox |
| `/profile` | All authenticated | User profile |
| `/exports` | All authenticated | Data exports |
| `/fitness/assessment` | Regular User | Fitness assessment |
| `/fitness/goals` | Regular User | Fitness goals |
| `/fitness/check-ins` | Regular User | Weekly check-ins |
| `/study/plans` | Regular User | Study plans |
| `/study/review` | Regular User | Review sessions |
| `/study/history` | Regular User | Study history |
| `/operations/receiving` | Operations | Receiving list |
| `/operations/receiving/:id` | Operations | Receipt detail |
| `/operations/discrepancies` | Operations | Discrepancies |
| `/operations/putaway` | Operations | Putaway queue |
| `/admin/master-data` | Admin | Master data listing |
| `/admin/master-data/import` | Admin | Bulk import |
| `/admin/master-data/merge` | Admin | Duplicate merge |
| `/admin/master-data/history` | Admin | Change history |
| `/admin/performance` | Admin | System performance |

## Scripts

```bash
npm run dev          # Start dev server (HTTP mode by default, set VITE_MOCK_MODE=true for mock)
npm run build        # Type-check and build for production
npm run preview      # Preview production build
npm test             # Run tests
npm run test:watch   # Run tests in watch mode
npm run test:coverage # Run tests with coverage
npm run type-check   # TypeScript type checking
```

## Docker

```bash
docker build -t campusfit-web .
docker run -p 3000:80 campusfit-web
```
