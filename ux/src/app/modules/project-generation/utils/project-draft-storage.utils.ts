export function loadProjectDraftFromStorage(projectId: string): any {
  const savedProjects = localStorage.getItem('projects');
  if (!savedProjects) {
    return null;
  }

  const projects = JSON.parse(savedProjects);
  return projects.find((project: any) => String(project.id) === String(projectId)) || null;
}

export function saveProjectDraftToStorage(projectData: any, backendProjectId: string | null, localProjectId: string | null): string {
  const savedProjects = localStorage.getItem('projects');
  const projects = savedProjects ? JSON.parse(savedProjects) : [];
  const persistedProjectId = backendProjectId || localProjectId;

  if (persistedProjectId) {
    const index = projects.findIndex((project: any) => String(project.id) === String(persistedProjectId));
    if (index !== -1) {
      projects[index] = { ...projectData, id: persistedProjectId };
    } else {
      projects.push({ ...projectData, id: persistedProjectId });
    }
    localStorage.setItem('projects', JSON.stringify(projects));
    return String(persistedProjectId);
  }

  const nextProjectId = String(Date.now());
  projects.push({ ...projectData, id: nextProjectId });
  localStorage.setItem('projects', JSON.stringify(projects));
  return nextProjectId;
}
