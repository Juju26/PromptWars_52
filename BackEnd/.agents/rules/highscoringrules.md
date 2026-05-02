---
trigger: always_on
---

For maximising the submission score you will be asked to check on the below points and generate  a detailed report of what can be improved. 


## Maximizing Your Assessment Score
1. Code Quality
Automated graders look for consistency, maintainability, and standard practices.

Linting & Formatting: Enforce strict linting rules (e.g., ESLint for JavaScript/TypeScript, PEP 8 for Python) from day one. The automated system will deduct points for messy code.

Modularity: Break your code down into small, reusable components. Instead of a massive file for the "Task Dashboard," have separate files for TaskCard, TaskList, and TaskFilter.

Type Safety: If possible, use strongly typed languages like TypeScript. This naturally reduces errors and signals high code quality to automated parsers.

2. Security
A team collaboration platform handles sensitive company data. Your code must reflect enterprise-grade security practices.

Input Validation: Sanitize all user inputs (chat messages, task descriptions) to prevent Cross-Site Scripting (XSS) and SQL Injection attacks.

Authentication & Authorization: Ensure API endpoints are protected. A user should not be able to view or edit a task on a project they haven't been granted access to.

Environment Variables: Never hardcode API keys or database credentials in your repository. Use .env files.

3. Efficiency
The grader will test how well your application handles data and rendering.

Database Query Optimization: Ensure you aren't over-fetching data. If the user only needs to see the task title and status on the dashboard, don't query and load the entire chat history for that task until they click on it.

Pagination & Lazy Loading: For the "Unified Inbox" or large task lists, implement pagination or infinite scroll so the system doesn't try to load thousands of records at once.

4. Testing
Automated platforms heavily weight test coverage. Do not leave this until the end.

Unit Tests: Write tests for your core logic (e.g., the function that calculates a "Project Health Score" or triggers a workflow automation).

Integration Tests: Ensure that your backend APIs communicate correctly with your database.

CI/CD Pipeline: If the platform allows, set up a simple GitHub Action to run your tests automatically on every commit.

5. Accessibility (a11y)
This is a frequent stumbling block in hackathons, but it's an easy way to score points if you plan for it.

Semantic HTML: Use proper tags (<nav>, <main>, <article>, <button>) instead of just using <div> for everything.

ARIA Attributes: Ensure screen readers can interpret dynamic status indicators and drag-and-drop Kanban boards.

Keyboard Navigation: A user should be able to navigate the entire task board and chat hub using only the Tab, Enter, and Arrow keys.

6. Google Services Usage
Since this is a specific evaluation criteria, you need to weave Google's ecosystem deeply into the platform.

Firebase: Use Firebase Realtime Database or Firestore to power the "Contextual Communication Hub" (the real-time task chats).

Google Cloud: Host your backend on Google Cloud Run or App Engine to demonstrate cloud proficiency.

Google Workspace APIs: Integrate Google Drive so users can attach Docs and Sheets directly to Nexus task cards.

Gemini API: Use Gemini to power the "Actionable Messages" feature, letting the AI extract task names, assignees, and deadlines from natural chat messages.

7. Problem Statement Alignment
Ensure every feature maps back to the core prompt: improving team coordination, simplifying workflows, and improving visibility. Don't get distracted building flashy features (like a custom avatar creator) if it doesn't directly solve the coordination problem.