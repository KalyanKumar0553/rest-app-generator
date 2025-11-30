import { Injectable } from '@angular/core';
import { createClient, SupabaseClient } from '@supabase/supabase-js';
import { environment } from '../../environments/environment';
import { ProjectData } from './project-data.service';

interface ProjectRecord {
  id: string;
  user_id: string;
  name: string;
  project_data: ProjectData;
  created_at: string;
  updated_at: string;
}

@Injectable({
  providedIn: 'root'
})
export class SupabaseProjectService {
  private supabase: SupabaseClient;

  constructor() {
    this.supabase = createClient(
      environment.supabaseUrl,
      environment.supabaseKey
    );
  }

  async saveProject(projectData: ProjectData, userId: string): Promise<{ success: boolean; id?: string; error?: string }> {
    try {
      const projectName = projectData.settings.projectName || 'Untitled Project';

      if (projectData.id) {
        const { data, error } = await this.supabase
          .from('projects')
          .update({
            name: projectName,
            project_data: projectData,
            updated_at: new Date().toISOString()
          })
          .eq('id', projectData.id)
          .eq('user_id', userId)
          .select()
          .maybeSingle();

        if (error) {
          console.error('Error updating project:', error);
          return { success: false, error: error.message };
        }

        return { success: true, id: data?.id };
      } else {
        const { data, error } = await this.supabase
          .from('projects')
          .insert({
            user_id: userId,
            name: projectName,
            project_data: projectData
          })
          .select()
          .maybeSingle();

        if (error) {
          console.error('Error creating project:', error);
          return { success: false, error: error.message };
        }

        return { success: true, id: data?.id };
      }
    } catch (error: any) {
      console.error('Exception saving project:', error);
      return { success: false, error: error.message };
    }
  }

  async loadProject(projectId: string, userId: string): Promise<{ success: boolean; data?: ProjectData; error?: string }> {
    try {
      const { data, error } = await this.supabase
        .from('projects')
        .select('*')
        .eq('id', projectId)
        .eq('user_id', userId)
        .maybeSingle();

      if (error) {
        console.error('Error loading project:', error);
        return { success: false, error: error.message };
      }

      if (!data) {
        return { success: false, error: 'Project not found' };
      }

      const projectData: ProjectData = {
        ...data.project_data,
        id: data.id
      };

      return { success: true, data: projectData };
    } catch (error: any) {
      console.error('Exception loading project:', error);
      return { success: false, error: error.message };
    }
  }

  async listProjects(userId: string): Promise<{ success: boolean; data?: any[]; error?: string }> {
    try {
      const { data, error } = await this.supabase
        .from('projects')
        .select('id, name, created_at, updated_at')
        .eq('user_id', userId)
        .order('updated_at', { ascending: false });

      if (error) {
        console.error('Error listing projects:', error);
        return { success: false, error: error.message };
      }

      return { success: true, data: data || [] };
    } catch (error: any) {
      console.error('Exception listing projects:', error);
      return { success: false, error: error.message };
    }
  }

  async deleteProject(projectId: string, userId: string): Promise<{ success: boolean; error?: string }> {
    try {
      const { error } = await this.supabase
        .from('projects')
        .delete()
        .eq('id', projectId)
        .eq('user_id', userId);

      if (error) {
        console.error('Error deleting project:', error);
        return { success: false, error: error.message };
      }

      return { success: true };
    } catch (error: any) {
      console.error('Exception deleting project:', error);
      return { success: false, error: error.message };
    }
  }
}
