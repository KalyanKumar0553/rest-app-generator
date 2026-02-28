import { NavItem } from '../../../../components/shared/sidenav/sidenav.component';
import { ModalButton } from '../../../../components/confirmation-modal/confirmation-modal.component';
import { RestEndpointConfig } from '../rest-config/rest-config.component';
import { DatabaseOption } from './project-generation-dashboard.models';
import { APP_SETTINGS } from '../../../../settings/app-settings';

// ─── Nav items ──────────────────────────────────────────────────────────────

export const BASE_NAV_ITEMS: NavItem[] = [
  { icon: 'public', label: 'General', value: 'general' },
  { icon: 'storage', label: 'Entities', value: 'entities' },
  { icon: 'category', label: 'Data Objects', value: 'data-objects' },
  { icon: 'search', label: 'Explore', value: 'explore' },
];

export const ACTUATOR_NAV_ITEM: NavItem = { icon: 'device_hub', label: 'Actuator', value: 'actuator' };
export const CONTROLLERS_NAV_ITEM: NavItem = { icon: 'tune', label: 'Controllers', value: 'controllers' };
export const MAPPERS_NAV_ITEM: NavItem = { icon: 'shuffle', label: 'Mappers', value: 'mappers' };

// ─── Form options ─────────────────────────────────────────────────────────────

export const FRONTEND_OPTIONS = ['None', 'React', 'Vue', 'Angular'];

export const DATABASE_OPTIONS: DatabaseOption[] = [
  { value: 'MSSQL', label: 'MSSQL Server', type: 'SQL' },
  { value: 'MYSQL', label: 'MySQL', type: 'SQL' },
  { value: 'MARIADB', label: 'MariaDB', type: 'SQL' },
  { value: 'ORACLE', label: 'Oracle', type: 'SQL' },
  { value: 'POSTGRES', label: 'PostgreSQL', type: 'SQL' },
  { value: 'DERBY', label: 'Apache Derby', type: 'SQL' },
  { value: 'H2', label: 'H2 Database', type: 'SQL' },
  { value: 'HSQL', label: 'HyperSQL', type: 'SQL' },
  { value: 'MONGODB', label: 'MongoDB', type: 'NOSQL' }
];

export const DB_TYPE_OPTIONS: Array<'SQL' | 'NOSQL' | 'NONE'> = ['SQL', 'NOSQL', 'NONE'];
export const DB_GENERATION_OPTIONS = ['Hibernate (update)', 'Hibernate (create)'];
export const JAVA_VERSION_OPTIONS = ['17', '21'];
export const DEPLOYMENT_OPTIONS = ['None', 'Docker', 'Kubernetes', 'Cloud'];

// ─── Default settings ─────────────────────────────────────────────────────────

export const DEFAULT_PROJECT_SETTINGS = {
  projectGroup: APP_SETTINGS.defaultProjectGroup,
  projectName: 'my-app',
  buildType: 'gradle' as const,
  language: 'java' as const,
  frontend: 'none'
};

export const DEFAULT_DATABASE_SETTINGS = {
  dbType: 'SQL' as const,
  database: 'POSTGRES',
  dbGeneration: 'Hibernate (update)',
  pluralizeTableNames: false
};

export const DEFAULT_DEVELOPER_PREFERENCES = {
  applFormat: 'yaml' as const,
  packages: 'technical' as const,
  enableOpenAPI: false,
  enableActuator: false,
  configureApi: true,
  enableLombok: false,
  useDockerCompose: false,
  profiles: [] as string[],
  javaVersion: '21',
  deployment: 'None'
};

// ─── Default controllers config ───────────────────────────────────────────────

export const DEFAULT_CONTROLLERS_CONFIG: RestEndpointConfig = {
  resourceName: '',
  basePath: '',
  mapToEntity: false,
  mappedEntityName: '',
  methods: {
    list: true,
    get: true,
    create: true,
    update: false,
    patch: true,
    delete: true,
    bulkInsert: true,
    bulkUpdate: true,
    bulkDelete: true
  },
  apiVersioning: {
    enabled: false,
    strategy: 'header',
    headerName: 'X-API-VERSION',
    defaultVersion: '1'
  },
  pathVariableType: 'UUID',
  deletion: {
    mode: 'SOFT',
    restoreEndpoint: true,
    includeDeletedParam: true
  },
  hateoas: {
    enabled: true,
    selfLink: true,
    updateLink: true,
    deleteLink: true
  },
  pagination: {
    enabled: true,
    mode: 'OFFSET',
    sortField: 'createdAt',
    sortDirection: 'DESC'
  },
  searchFiltering: {
    keywordSearch: true,
    jpaSpecification: true,
    searchableFields: []
  },
  batchOperations: {
    insert: {
      batchSize: 500,
      enableAsyncMode: false
    },
    update: {
      batchSize: 500,
      updateMode: 'PUT',
      optimisticLockHandling: 'FAIL_ON_CONFLICT',
      validationStrategy: 'VALIDATE_ALL_FIRST',
      enableAsyncMode: false,
      asyncProcessing: true
    },
    bulkDelete: {
      deletionStrategy: 'SOFT',
      batchSize: 1000,
      failureStrategy: 'STOP_ON_FIRST_ERROR',
      enableAsyncMode: false,
      allowIncludeDeletedParam: true
    }
  },
  requestResponse: {
    request: {
      list: { mode: 'GENERATE_DTO', dtoName: '' },
      create: { mode: 'GENERATE_DTO', dtoName: '' },
      delete: { mode: 'GENERATE_DTO', dtoName: '' },
      update: { mode: 'GENERATE_DTO', dtoName: '' },
      patch: { mode: 'JSON_MERGE_PATCH' },
      getByIdType: 'UUID',
      deleteByIdType: 'UUID',
      bulkInsertType: '',
      bulkUpdateType: '',
      bulkDeleteType: ''
    },
    response: {
      responseType: 'RESPONSE_ENTITY',
      dtoName: '',
      endpointDtos: {
        list: '',
        get: '',
        create: '',
        update: '',
        patch: '',
        delete: '',
        bulkInsert: '',
        bulkUpdate: '',
        bulkDelete: ''
      },
      responseWrapper: 'STANDARD_ENVELOPE',
      enableFieldProjection: true,
      includeHateoasLinks: true
    }
  },
  documentation: {
    includeDefaultDocumentation: true,
    endpoints: {
      list: { description: 'List operation for API', group: 'API Group', descriptionTags: ['list'], deprecated: false },
      get: { description: 'Get By Key operation for API', group: 'API Group', descriptionTags: ['get'], deprecated: false },
      create: { description: 'Create operation for API', group: 'API Group', descriptionTags: ['create'], deprecated: false },
      update: { description: 'Update operation for API', group: 'API Group', descriptionTags: ['update'], deprecated: false },
      patch: { description: 'Patch operation for API', group: 'API Group', descriptionTags: ['patch'], deprecated: false },
      delete: { description: 'Delete operation for API', group: 'API Group', descriptionTags: ['delete'], deprecated: false },
      bulkInsert: { description: 'Bulk Insert operation for API', group: 'API Group', descriptionTags: ['bulkInsert'], deprecated: false },
      bulkUpdate: { description: 'Bulk Update operation for API', group: 'API Group', descriptionTags: ['bulkUpdate'], deprecated: false },
      bulkDelete: { description: 'Bulk Delete operation for API', group: 'API Group', descriptionTags: ['bulkDelete'], deprecated: false }
    }
  }
};

// ─── Confirmation modal configs ───────────────────────────────────────────────

export const BACK_CONFIRMATION_CONFIG = {
  title: 'Unsaved Changes',
  message: ['You have unsaved changes. All changes will be discarded if you leave this page.Are you sure you want to continue ?'],
  buttons: [
    { text: 'Cancel', type: 'cancel' as const, action: 'cancel' as const },
    { text: 'Discard Changes', type: 'danger' as const, action: 'confirm' as const }
  ] as ModalButton[]
};

export const ENTITIES_DELETE_CONFIRMATION_CONFIG: { title: string; message: string; buttons: ModalButton[] } = {
  title: 'Confirmation',
  message: 'All Configured Entities will be deleted. Want to Continue ?',
  buttons: [
    { text: 'Continue', type: 'danger', action: 'confirm' },
    { text: 'Cancel', type: 'cancel', action: 'cancel' }
  ]
};

export const REST_SPEC_DELETE_CONFIRMATION_CONFIG: { title: string; message: string; buttons: ModalButton[] } = {
  title: 'Delete REST Configuration',
  message: 'This will remove REST endpoint configuration and related model mapping.',
  buttons: [
    { text: 'Delete', type: 'danger', action: 'confirm' },
    { text: 'Cancel', type: 'cancel', action: 'cancel' }
  ]
};

export const CONFIGURE_API_DISABLE_CONFIRMATION_CONFIG: { title: string; message: string; buttons: ModalButton[] } = {
  title: 'Confirmation',
  message: 'All API configuration will be lost. Want to proceed ?',
  buttons: [
    { text: 'Continue', type: 'danger', action: 'confirm' },
    { text: 'Cancel', type: 'cancel', action: 'cancel' }
  ]
};

export const CONTROLLERS_CONFIG_DISCARD_CONFIRMATION_CONFIG: { title: string; message: string; buttons: ModalButton[] } = {
  title: 'Confirmation',
  message: 'All Change will be discarded from the configuration. Want to proceed ?',
  buttons: [
    { text: 'Confirm', type: 'danger', action: 'confirm' },
    { text: 'Cancel', type: 'cancel', action: 'cancel' }
  ]
};

export const GENERATION_CANCEL_CONFIRMATION_CONFIG: { title: string; message: string; buttons: ModalButton[] } = {
  title: 'Cancel Generation',
  message: 'Project generation is in progress. Do you want to cancel?',
  buttons: [
    { text: 'Confirm', type: 'danger', action: 'confirm' },
    { text: 'Cancel', type: 'confirm', action: 'cancel' }
  ]
};

export const RECENT_PROJECT_PROMPT_CONFIG: { title: string; message: string; buttons: ModalButton[] } = {
  title: 'Load Recent project',
  message: "You've worked on an another project before. Would you like to continue there?",
  buttons: [
    { text: 'Resume Last Project', type: 'confirm', action: 'confirm' },
    { text: 'Create New project', type: 'cancel', action: 'cancel' }
  ]
};
