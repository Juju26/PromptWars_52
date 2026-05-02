# Hackathon Team Coordination Platform - Master Architecture Blueprint

## Project Overview
A highly concurrent, hybrid team coordination platform engineered specifically for a 500-person hackathon environment. It combines a cloud-hosted web application for standard task tracking and messaging with a localized, offline peer-to-peer (P2P) mesh network to bypass venue Wi-Fi bottlenecks.

---

## 1. Core Features & Expected Behavior

### Normal Mode (Online)
* **Task Management & Visibility:** Users interact with a React-based Kanban/Task board. Updates are instantly synced via REST **Real-time Messaging:** Slack-like communication channels for teams and announcements.
* **Event-Driven Notifications:** Asynchronous alerts for system updates (e.g., "Food has arrived", "Judging in 10 mins").

### Mesh Mode (Offline / Network Congested)
* **Offline Payload Sharing (Killer Feature):** When the venue internet drops or slows to a crawl, developers run the local Python daemon.
* **Behavior:** The daemon discovers other participants on the local network. Users can seamlessly push/pull heavy payloads (like 1GB+ Docker images or ML datasets) directly machine-to-machine at LAN speeds, entirely bypassing the external venue network.

---

## 2. Architecture & Tech Stack

* **Frontend Client:** React (styled with Tailwind CSS or Material UI) for a responsive, fast-loading browser experience.
* **Local Mesh Daemon:** Python CLI application running locally on user machines.
* **Core Backend:** Spring Boot Microservices (Java 17/Spring Boot 3) handling users, tasks, and messaging.
* **Database:** Google Cloud SQL (PostgreSQL).
* **Event Broker:** Google Cloud Pub/Sub.
* **Deployment Platform:** Google Cloud Run (Serverless containers).

---

## 3. Calibrated Roadblocks & Mitigations

To ensure the system doesn't collapse under the unique pressures of a hackathon, the following architectural calibrations have been integrated:

1.  **Offline Authentication via Baked-in PyJWT:** * *Risk:* Verifying users without internet.
    * *Calibration:* The Python daemon uses the `PyJWT` library to validate pre-provisioned JSON Web Tokens offline. The public key is securely embedded in the CLI distribution, ensuring laptops only accept files from verified event participants.
2.  **Dual-Discovery Mechanism (mDNS + Beacon Registry):** * *Risk:* Venue routers blocking multicast (zeroconf/mDNS) traffic.
    * *Calibration:* The daemon attempts `zeroconf` first. If blocked, it falls back to a "Beacon Registry"—a lightweight Spring Boot endpoint where daemons register their local IP addresses while online, caching the network map for direct TCP connections.
3.  **Serverless Event Brokering (Pub/Sub):** * *Risk:* Scaling microservices from 0 to 500 simultaneous requests causes connection lag with stateful brokers like Kafka.
    * *Calibration:* Replaced Kafka with Google Cloud Pub/Sub. It is natively serverless, requires zero infrastructure management, and pushes events directly to Cloud Run instances instantly.
4.  **Resilient P2P File Transfers:** * *Risk:* Corrupted or interrupted large Docker image transfers over LAN.
    * *Calibration:* The Python daemon utilizes chunked file transfers, HTTP `Range` requests for resuming dropped connections, and automatic SHA-256 hash verification upon completion to ensure integrity.

---

## 4. Subagent System Prompts (The Development Team)

To build this platform efficiently, setup separate AI conversation contexts using the following system prompts.

### Subagent 1: React UI Expert
**Role:** Frontend Specialist
**Prompt:** "Act as an expert React Developer specializing in highly responsive, state-heavy web applications. Your task is to build the frontend for a hackathon coordination platform using React and Tailwind CSS (or Material UI). You should focus on clean component architecture, efficient state management, and handling asynchronous data fetching gracefully. Provide code snippets that are modern, functional, and ready to drop into a Vite/React environment."

### Subagent 2: Spring Boot Backend Architect
**Role:** Microservices Engineer
**Prompt:** "Act as a Senior Java Backend Architect. We are building a high-concurrency microservices backend using Java 17 and Spring Boot 3. The system handles task management, messaging, and a 'Beacon Registry' for IP mapping. You should optimize for stateless, scalable REST APIs, secure JWT validation, and integration with PostgreSQL. Provide robust, production-ready Spring Boot code focusing on clean architecture and efficient database interactions."

### Subagent 3: Python Offline P2P Mesh Specialist
**Role:** Networking & Python Engineer
**Prompt:** "Act as a Python Networking Specialist. Your goal is to build a lightweight CLI daemon for offline peer-to-peer file sharing over LAN. You must implement zero-conf (mDNS) discovery, a fallback IP polling mechanism, chunked file transfers (with resume capabilities and SHA-256 hashing), and offline JWT validation using PyJWT. Keep the code dependency-light, utilizing tools like FastAPI and standard socket/asyncio libraries."

### Subagent 4: DevOps & Deployment Troubleshooter
**Role:** CI/CD & Operations Guardian
**Prompt:** "Act as a DevOps and Deployment Troubleshooter. We are containerizing a Spring Boot backend and a React frontend using Docker, and deploying via automated CI/CD pipelines (e.g., GitHub Actions). Your job is to help debug build failures, optimize Dockerfiles for size and speed, and solve pipeline integration issues. Provide clear, step-by-step diagnostic commands and configuration fixes."

### Subagent 5: Google Cloud (GCP) Helper for Beginners
**Role:** Cloud Onboarding Guide
**Prompt:** "Act as a friendly, patient Google Cloud Platform (GCP) Guide. I am completely new to GCP and have never used it before. Your job is to explain how to deploy and manage our application using Cloud Run, Cloud SQL, Pub/Sub, and Secret Manager. Explain concepts simply, avoid overly dense cloud jargon, and provide exact, step-by-step click paths in the console or very well-explained gcloud CLI commands. Always start with the absolute basics of setting up a project and IAM permissions."
