import { Component, computed, effect, inject, signal, ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';

import { FormsModule } from '@angular/forms';

import { HttpErrorResponse } from '@angular/common/http';

import { LoginComponent } from './features/login/login.component';

import { AuthService } from './core/auth.service';

import { BackendColony, ColonyService } from './core/colony.service';

import { BackendCat, CatPayload, CatService } from './core/cat.service';

import { BackendUser, UserPayload, UserUpdatePayload, UserService } from './core/user.service';

import {
  BackendIncident,
  IncidentPayload,
  IncidentService,
  IncidentType,
  IncidentPriority,
  IncidentStatus,
} from './core/incident.service';

import {
  BackendTarea,
  TareaPayload,
  TareaService,
  TareaTipo,
  TareaEstado,
} from './core/tarea.service';

type CatFilter = 'Todos' | 'Esterilizados' | 'Pendientes';

type Section =
  'Inicio' | 'Colonias' | 'Gatos' | 'Incidencias' | 'Voluntariado' | 'Calendario' | 'Perfil';

type View = 'Login' | Section;

interface ColonyView {
  id: number;

  name: string;

  location: string;

  cats: number;

  active: boolean;

  image: string;
}

interface CatView {
  id: number;

  name: string | null;

  code: string;

  colony: string;

  color: string;

  status: string;

  sterilized: boolean;
}

interface ColonyForm {
  id: number | null;

  name: string;

  description: string;

  location: string;

  active: boolean;
}

interface CatForm {
  id: number | null;

  name: string;

  sex: string;

  birthDate: string;

  color: string;

  status: string;

  neutered: boolean;

  identified: boolean;

  colonyId: number | null;

  observations: string;
}

interface UserForm {
  id: number | null;

  nombre: string;

  email: string;

  password: string;

  rol: string;

  activo: boolean;
}

interface IncidentForm {
  id: number | null;

  titulo: string;

  descripcion: string;

  tipo: IncidentType;

  prioridad: IncidentPriority;

  estado: IncidentStatus;

  catId: number | null;

  colonyId: number | null;
}

type TaskType = TareaTipo;
type TaskStatus = TareaEstado;

interface TaskForm {
  id: number | null;
  titulo: string;
  descripcion: string;
  tipo: TaskType;
  estado: TaskStatus;
  fechaHora: string;
  catId: number | null;
  colonyId: number | null;
}

const PLACEHOLDER_IMAGE =
  'https://images.unsplash.com/photo-1573865526739-10659fec78a5?auto=format&fit=crop&w=600&q=80';

@Component({
  selector: 'app-root',

  standalone: true,

  imports: [CommonModule, FormsModule, LoginComponent],

  templateUrl: './app.component.html',

  styleUrl: './app.component.css',
})
export class AppComponent {
  private readonly auth = inject(AuthService);

  private readonly colonyService = inject(ColonyService);

  private readonly catService = inject(CatService);

  private readonly userService = inject(UserService);

  private readonly incidentService = inject(IncidentService);

  private readonly tareaService = inject(TareaService);

  private readonly changeDetector = inject(ChangeDetectorRef);

  readonly usuario = this.auth.usuario;

  readonly cargandoSesion = this.auth.cargando;

  activeView: View = 'Login';

  searchTerm = '';

  private readonly rawColonies = signal<BackendColony[]>([]);

  private readonly rawCats = signal<BackendCat[]>([]);

  readonly users = signal<BackendUser[]>([]);

  usersMessage: string | null = null;

  // Incidencias

  private readonly rawIncidents = signal<BackendIncident[]>([]);

  private readonly rawTasks = signal<BackendTarea[]>([]);

  incidentFormOpen = false;

  incidentForm: IncidentForm = this.emptyIncidentForm();

  taskFormOpen = false;

  taskForm: TaskForm = this.emptyTaskForm();

  savingTask = false;

  taskFieldErrors: Partial<Record<keyof TaskForm, string>> = {};

  // Datos derivados como signals: los arrays base deben ser signals también; si son

  // propiedades corrientes, el computed no se entera de su cambio y mostraría siempre 0.

  private readonly colonyCountSignal = computed(() => this.rawColonies().length);

  private readonly activeColonyCountSignal = computed(
    () => this.rawColonies().filter((c) => c.active).length,
  );

  private readonly catCountSignal = computed(() => this.rawCats().length);

  private readonly adoptionCountSignal = computed(
    () => this.rawCats().filter((c) => c.status === 'EN_ADOPCION').length,
  );

  private readonly userCountSignal = computed(() => this.users().length);

  // Formularios

  colonyFormOpen = false;

  catFormOpen = false;

  userFormOpen = false;

  formError: string | null = null;

  colonyForm: ColonyForm = this.emptyColonyForm();

  catForm: CatForm = this.emptyCatForm();

  userForm: UserForm = this.emptyUserForm();

  catColonyValidationError = false;

  // Guardas de envío: evitan que varios clics dispare varias peticiones y duplique el registro.

  savingColony = false;

  savingCat = false;

  savingUser = false;

  deletingUserId: number | null = null;

  savingIncident = false;

  // Todas las entradas del menú; se filtran según el rol al mostrarlas.

  private readonly allNavigation: { label: Section; icon: string; adminOnly?: boolean }[] = [
    { label: 'Inicio', icon: '⌂' },

    { label: 'Colonias', icon: '♧' },

    { label: 'Gatos', icon: '♢' },

    { label: 'Incidencias', icon: '⚑' },

    { label: 'Voluntariado', icon: '♙', adminOnly: true },

    { label: 'Calendario', icon: '▦' },

    { label: 'Perfil', icon: '◉' },
  ];

  /**
   * Entradas que se muestran en el menú lateral.
   * Voluntariado solo está disponible para ADMIN.
   */
  get navigation(): { label: Section; icon: string }[] {
    return this.allNavigation

      .filter((item) => !item.adminOnly || this.esAdministrador())

      .map((item) => ({ label: item.label, icon: item.icon }));
  }

  esAdministrador(): boolean {
    return this.usuario()?.rol === 'ADMIN';
  }

  trackByItem(index: number, item: any): number | string {
    if (item === null || item === undefined) {
      return index;
    }

    if (typeof item !== 'object') {
      return item;
    }

    return item.id ?? item.label ?? item.name ?? item.value ?? index;
  }

  readonly subtitles: Record<Section, string> = {
    Inicio: 'Esto es lo que está pasando hoy en tus colonias.',

    Colonias: 'Todas las colonias que gestionas y su estado.',

    Gatos: 'Fichas de los gatos, incluidos los que aún no tienen nombre.',

    Voluntariado: 'Las personas con acceso a la plataforma y su rol.',

    Calendario: 'Visitas, alimentación y revisiones previstas.',

    Perfil: 'Tus datos y los permisos de cada rol.',

    Incidencias: 'Registra y consulta los problemas detectados en las colonias.',
  };

  readonly catFilters: CatFilter[] = ['Todos', 'Esterilizados', 'Pendientes'];

  catFilter: CatFilter = 'Todos';

  readonly statusLabels: Record<string, string> = {
    EN_COLONIA: 'En colonia',

    EN_SEGUIMIENTO: 'En seguimiento',

    EN_ADOPCION: 'En adopción',
  };

  readonly statusOptions = Object.keys(this.statusLabels);

  readonly sexOptions = [
    { value: 'FEMALE', label: 'Hembra' },

    { value: 'MALE', label: 'Macho' },

    { value: 'UNKNOWN', label: 'Desconocido' },
  ];

  readonly roleLabels: Record<string, string> = {
    ADMIN: 'Administración',

    CUIDADOR: 'Cuidador/a',

    CONSULTA: 'Consulta',
  };

  readonly rolOptions = [
    { value: 'CUIDADOR', label: 'Cuidador/a' },

    { value: 'CONSULTA', label: 'Consulta' },

    { value: 'ADMIN', label: 'Administración' },
  ];

  readonly incidentTypeLabels: Record<IncidentType, string> = {
    SALUD: 'Salud',

    ALIMENTACION: 'Alimentación',

    ABANDONO: 'Abandono',

    SEGURIDAD: 'Seguridad',

    OTRO: 'Otro',
  };

  readonly incidentPriorityLabels: Record<IncidentPriority, string> = {
    BAJA: 'Baja',

    MEDIA: 'Media',

    ALTA: 'Alta',

    URGENTE: 'Urgente',
  };

  readonly incidentStatusLabels: Record<IncidentStatus, string> = {
    ABIERTA: 'Abierta',

    EN_PROCESO: 'En proceso',

    RESUELTA: 'Resuelta',

    CERRADA: 'Cerrada',
  };

  readonly incidentTypeOptions: IncidentType[] = [
    'SALUD',
    'ALIMENTACION',
    'ABANDONO',
    'SEGURIDAD',
    'OTRO',
  ];

  readonly incidentPriorityOptions: IncidentPriority[] = ['BAJA', 'MEDIA', 'ALTA', 'URGENTE'];

  readonly incidentStatusOptions: IncidentStatus[] = [
    'ABIERTA',
    'EN_PROCESO',
    'RESUELTA',
    'CERRADA',
  ];

  readonly tareaTipoLabels: Record<TaskType, string> = {
    ALIMENTACION: 'Alimentación',
    VISITA: 'Visita',
    VETERINARIO: 'Veterinario',
    REVISION: 'Revisión',
    OTRO: 'Otro',
  };

  readonly tareaEstadoLabels: Record<TaskStatus, string> = {
    PENDIENTE: 'Pendiente',
    COMPLETADA: 'Completada',
  };

  readonly tareaTipoOptions: TaskType[] = [
    'ALIMENTACION',
    'VISITA',
    'VETERINARIO',
    'REVISION',
    'OTRO',
  ];

  readonly roles = [
    { name: 'Administración', description: 'Gestiona usuarios, colonias y gatos.' },

    { name: 'Cuidador/a', description: 'Registra y actualiza colonias y gatos.' },

    { name: 'Consulta', description: 'Solo puede consultar la información.' },
  ];

  readonly tareaEstadoOptions: TaskStatus[] = ['PENDIENTE', 'COMPLETADA'];

  readonly now = new Date();

  tasksLoadError: string | null = null;

  readonly todayLabel = this.now.toLocaleDateString('es-ES', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
  });

  readonly weekDays = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

  taskCalendarMonth = new Date(this.now.getFullYear(), this.now.getMonth(), 1);

  taskSelectedDate = this.localDateKey(this.now);

  get todayDay(): number {
    return this.now.getDate();
  }

  get calendarMonth(): string {
    return this.taskCalendarMonth.toLocaleDateString('es-ES', { month: 'long', year: 'numeric' });
  }

  get calendarWeeks(): (number | null)[][] {
    return this.buildCalendar(
      this.taskCalendarMonth.getFullYear(),
      this.taskCalendarMonth.getMonth(),
    );
  }

  get selectedDayTasks(): BackendTarea[] {
    if (!this.taskSelectedDate) return [];

    const selected = this.selectedCalendarDate();
    if (!selected) return [];

    const selectedKey = this.localDateKey(selected);
    return this.rawTasks().filter((task) => this.localDateKey(task.fechaHora) === selectedKey);
  }

  get upcomingTasks(): BackendTarea[] {
    return [...this.rawTasks()]
      .filter((task) => new Date(task.fechaHora).getTime() >= new Date().setHours(0, 0, 0, 0))
      .sort((a, b) => new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime())
      .slice(0, 6);
  }

  constructor() {
    effect(() => {
      if (this.auth.cargando()) return;

      const user = this.auth.usuario();

      if (user) {
        if (this.activeView === 'Login') this.activeView = 'Inicio';

        this.loadColonies();

        this.loadCats();

        this.loadIncidents();

        this.loadTasks();

        this.loadUsers(user.rol);
      } else {
        this.activeView = 'Login';

        this.rawColonies.set([]);

        this.rawCats.set([]);

        this.rawIncidents.set([]);

        this.rawTasks.set([]);

        this.users.set([]);
      }
    });
  }

  // ---------- Datos derivados ----------

  get colonyCount(): number {
    return this.colonyCountSignal();
  }

  get catCount(): number {
    return this.catCountSignal();
  }

  get stats() {
    return [
      {
        label: 'Colonias activas',
        value: String(this.activeColonyCountSignal()),
        icon: '⌂',
        tone: 'violet',
      },

      { label: 'Gatos registrados', value: String(this.catCountSignal()), icon: '♧', tone: 'pink' },

      {
        label: 'Personas con acceso',
        value: String(this.userCountSignal()),
        icon: '♙',
        tone: 'blue',
      },

      { label: 'En adopción', value: String(this.adoptionCountSignal()), icon: '♡', tone: 'peach' },
    ];
  }

  get filteredColonies(): ColonyView[] {
    const term = this.searchTerm.trim().toLocaleLowerCase('es');

    return this.rawColonies()

      .map((c) => ({
        id: c.id,

        name: c.name,

        location: c.location,

        active: c.active,

        cats: this.rawCats().filter((cat) => cat.colonyId === c.id).length,

        image: PLACEHOLDER_IMAGE,
      }))

      .filter(
        (c) =>
          !term ||
          c.name.toLocaleLowerCase('es').includes(term) ||
          c.location.toLocaleLowerCase('es').includes(term),
      );
  }

  get colonyOptions(): { id: number; name: string }[] {
    return this.rawColonies()
      .filter((c) => c.active)
      .map((c) => ({ id: c.id, name: c.name }));
  }

  get filteredIncidents(): BackendIncident[] {
    const term = this.searchTerm.trim().toLocaleLowerCase('es');

    return this.rawIncidents().filter((incident) => {
      const text = [
        incident.titulo,

        incident.descripcion,

        this.incidentTypeLabels[incident.tipo],

        this.incidentPriorityLabels[incident.prioridad],

        this.incidentStatusLabels[incident.estado],

        incident.catName ?? '',

        incident.colonyName ?? '',
      ]
        .join(' ')
        .toLocaleLowerCase('es');

      return !term || text.includes(term);
    });
  }

  get puedeGestionarIncidencias(): boolean {
    const rol = this.usuario()?.rol;

    return rol === 'ADMIN' || rol === 'CUIDADOR';
  }

  get incidentSubmitLabel(): string {
    const action = this.incidentForm.id === null ? 'Creando' : 'Guardando';

    return this.savingIncident ? `${action} incidencia...` : 'Guardar incidencia';
  }

  get filteredCats(): CatView[] {
    const term = this.searchTerm.trim().toLocaleLowerCase('es');

    return this.rawCats()

      .map<CatView>((cat) => ({
        id: cat.id,

        name: cat.name,

        code: '#' + String(cat.id).padStart(3, '0'),

        colony: cat.colonyName ?? 'Sin asignar',

        color: cat.color ?? '—',

        status: this.statusLabels[cat.status] ?? cat.status,

        sterilized: !!cat.neutered,
      }))

      .filter((cat) => {
        const matchesFilter =
          this.catFilter === 'Todos' || (this.catFilter === 'Esterilizados') === cat.sterilized;

        const text = `${cat.name ?? 'Sin nombre'} ${cat.code} ${cat.colony}`.toLocaleLowerCase(
          'es',
        );

        return matchesFilter && (!term || text.includes(term));
      });
  }

  // Etiquetas del botón de envío: cambian a "Creando/Guardando ..." mientras hay petición en marcha.

  get colonySubmitLabel(): string {
    const action = this.colonyForm.id === null ? 'Creando' : 'Guardando';

    return this.savingColony ? `${action} colonia...` : `Guardar colonia`;
  }

  get catSubmitLabel(): string {
    const action = this.catForm.id === null ? 'Creando' : 'Guardando';

    return this.savingCat ? `${action} gato...` : `Guardar gato`;
  }

get userSubmitLabel(): string {
  if (this.savingUser) {
    return this.userForm.id === null
      ? 'Creando acceso...'
      : 'Guardando cambios...';
  }

  return this.userForm.id === null
    ? 'Crear acceso'
    : 'Guardar cambios';
}

  roleLabel(rol: string | undefined): string {
    return rol ? (this.roleLabels[rol] ?? rol) : '';
  }

  roleClass(rol: string): string {
    return rol === 'ADMIN' ? 'admin' : rol === 'CONSULTA' ? 'viewer' : '';
  }

  // ---------- Navegación ----------

  selectSection(section: Section): void {
    // Barrera adicional: sin rol de administración no se puede abrir el voluntariado.

    if (section === 'Voluntariado' && !this.esAdministrador()) return;

    this.activeView = section;

    this.searchTerm = '';

    this.catFilter = 'Todos';

    this.closeForms();
  }

  login(): void {
    this.activeView = 'Inicio';
  }

  logout(): void {
    this.auth.logout().subscribe({ error: () => this.auth.usuario.set(null) });
  }

  // ---------- Colonias ----------

  newColony(): void {
    this.selectSection('Colonias');

    this.colonyForm = this.emptyColonyForm();

    this.colonyFormOpen = true;
  }

  editColony(colony: ColonyView): void {
    const raw = this.rawColonies().find((c) => c.id === colony.id);

    if (!raw) return;

    this.closeForms();

    this.colonyForm = {
      id: raw.id,

      name: raw.name,

      description: raw.description ?? '',

      location: raw.location,

      active: raw.active,
    };

    this.colonyFormOpen = true;
  }

  saveColony(): void {
    if (this.savingColony) return;

    const f = this.colonyForm;

    // Validar los campos obligatorios

    if (!f.name.trim() || !f.location.trim()) {
      this.formError = 'El nombre y la ubicación son obligatorios.';

      return;
    }

    const payload = {
      name: f.name.trim(),

      description: f.description.trim() || null,

      location: f.location.trim(),

      active: f.active,
    };

    const request =
      f.id === null
        ? this.colonyService.createColony(payload)
        : this.colonyService.updateColony(f.id, payload);

    this.savingColony = true;

    this.formError = null;

    request.subscribe({
      next: () => {
        this.savingColony = false;

        this.closeForms();

        this.loadColonies();
      },

      error: (e: HttpErrorResponse) => {
        this.savingColony = false;

        this.formError = this.errorText(e);

        // El error llega fuera del ciclo de detección de cambios (XHR responde en un

        // macro-task y el bloque del alerta es una vista embebida desmontada). Sin esto,

        // el mensaje no se pinta hasta que el usuario clica en otra parte.

        this.changeDetector.markForCheck();
      },
    });
  }

  archiveColony(colony: ColonyView): void {
    this.colonyService.archiveColony(colony.id).subscribe({
      next: () => this.loadColonies(),

      error: (e: HttpErrorResponse) => (this.formError = this.errorText(e)),
    });
  }

  // ---------- Gatos ----------

  newCat(): void {
    this.selectSection('Gatos');

    this.catForm = this.emptyCatForm();

    this.catColonyValidationError = false;

    this.catFormOpen = true;
  }

  editCat(cat: CatView): void {
    const raw = this.rawCats().find((c) => c.id === cat.id);

    if (!raw) return;

    this.closeForms();

    this.catColonyValidationError = false;

    this.catForm = {
      id: raw.id,

      name: raw.name ?? '',

      sex: raw.sex ?? 'UNKNOWN',

      birthDate: raw.birthDate ?? '',

      color: raw.color ?? '',

      status: raw.status,

      neutered: !!raw.neutered,

      identified: !!raw.identified,

      colonyId: raw.colonyId,

      observations: raw.observations ?? '',
    };

    this.catFormOpen = true;
  }

  saveCat(): void {
    if (this.savingCat) return;

    this.formError = null;

    this.catColonyValidationError = this.requiresCatColony && this.catForm.colonyId == null;

    if (this.catColonyValidationError) {
      return;
    }

    const f = this.catForm;

    const payload: CatPayload = {
      name: f.name.trim() || null,

      sex: f.sex,

      birthDate: f.birthDate || null,

      color: f.color.trim() || null,

      status: f.status,

      neutered: f.neutered,

      identified: f.identified,

      colonyId: f.colonyId ?? null,

      observations: f.observations.trim() || null,
    };

    const request =
      f.id === null ? this.catService.createCat(payload) : this.catService.updateCat(f.id, payload);

    this.savingCat = true;

    this.formError = null;

    request.subscribe({
      next: () => {
        this.savingCat = false;
        this.closeForms();
        this.loadCats();
      },

      error: (e: HttpErrorResponse) => {
        this.savingCat = false;

        this.formError = this.errorText(e);
      },
    });
  }

  deleteCat(cat: CatView): void {
    if (!confirm(`¿Eliminar a ${cat.name ?? 'este gato'}?`)) return;

    this.catService.deleteCat(cat.id).subscribe({
      next: () => this.loadCats(),

      error: (e: HttpErrorResponse) => (this.formError = this.errorText(e)),
    });
  }

  closeForms(): void {
    this.colonyFormOpen = false;

    this.catFormOpen = false;

    this.userFormOpen = false;

    this.incidentFormOpen = false;

    this.taskFormOpen = false;

    this.taskFieldErrors = {};

    this.formError = null;

    this.catColonyValidationError = false;
  }

  // ---------- Voluntariado ----------

  newUser(): void {
    this.selectSection('Voluntariado');

    this.userForm = this.emptyUserForm();

    this.userFormOpen = true;
  }

  editUser(person: BackendUser): void {
  if (!this.esAdministrador()) return;

  this.closeForms();

  this.userForm = {
    id: person.id,
    nombre: person.nombre,
    email: person.email,
    password: '',
    rol: person.rol,
    activo: person.activo
  };

  this.userFormOpen = true;
}

saveUser(): void {
  if (this.savingUser) return;

  if (!this.esAdministrador()) {
    this.formError = 'Solo la administración puede gestionar cuentas.';
    return;
  }

  const f = this.userForm;

  if (!f.nombre.trim() || !f.email.trim()) {
    this.formError = 'El nombre y el correo son obligatorios.';
    return;
  }

  if (f.id === null && !f.password.trim()) {
    this.formError = 'La contraseña es obligatoria al crear una cuenta.';
    return;
  }

  const payload = {
    nombre: f.nombre.trim(),
    email: f.email.trim(),
    rol: f.rol,
    activo: f.activo
  };

  this.savingUser = true;
  this.formError = null;

  const request = f.id === null
    ? this.userService.createUser({
        ...payload,
        password: f.password
      })
    : this.userService.updateUser(f.id, {
        ...payload,
        password: f.password.trim() || null
      });

  request.subscribe({
    next: () => {
      this.savingUser = false;
      this.closeForms();
      this.loadUsers(this.usuario()!.rol);
    },

    error: (e: HttpErrorResponse) => {
      this.savingUser = false;
      this.formError = this.errorText(e);
    }
  });
}


  deleteUser(person: BackendUser): void {
    if (!this.esAdministrador()) {
      this.formError = 'Solo la administración puede eliminar usuarios.';

      return;
    }

    if (person.id === this.usuario()?.id) {
      this.formError = 'No puedes eliminar tu propia cuenta.';

      return;
    }

    if (this.deletingUserId !== null) return;

    const confirmado = confirm(`¿Seguro que quieres eliminar la cuenta de ${person.nombre}?`);

    if (!confirmado) return;

    this.deletingUserId = person.id;

    this.formError = null;

    this.userService.deleteUser(person.id).subscribe({
      next: () => {
        this.users.update((users) => users.filter((user) => user.id !== person.id));

        this.deletingUserId = null;
      },

      error: (e: HttpErrorResponse) => {
        this.deletingUserId = null;

        this.formError = this.errorText(e);
      },
    });
  }

  // ---------- Incidencias ----------

  newIncident(): void {
    if (!this.puedeGestionarIncidencias) {
      this.formError = 'No tienes permisos para crear incidencias.';

      return;
    }

    this.selectSection('Incidencias');

    this.incidentForm = this.emptyIncidentForm();

    this.incidentFormOpen = true;
  }

  editIncident(incident: BackendIncident): void {
    if (!this.puedeGestionarIncidencias) return;

    this.closeForms();

    this.incidentForm = {
      id: incident.id,

      titulo: incident.titulo ?? '',

      descripcion: incident.descripcion ?? '',

      tipo: incident.tipo,

      prioridad: incident.prioridad,

      estado: incident.estado,

      catId: incident.catId ?? null,

      colonyId: incident.colonyId ?? null,
    };

    this.incidentFormOpen = true;
  }

  saveIncident(): void {
    if (this.savingIncident) return;

    if (!this.puedeGestionarIncidencias) {
      this.formError = 'No tienes permisos para gestionar incidencias.';

      return;
    }

    const f = this.incidentForm;

    if (!f.titulo.trim() || !f.descripcion.trim()) {
      this.formError = 'El título y la descripción son obligatorios.';

      return;
    }

    const payload: IncidentPayload = {
      titulo: f.titulo.trim(),

      descripcion: f.descripcion.trim(),

      tipo: f.tipo,

      prioridad: f.prioridad,

      estado: f.estado,

      catId: f.catId,

      colonyId: f.colonyId,
    };

    const request =
      f.id === null
        ? this.incidentService.createIncident(payload)
        : this.incidentService.updateIncident(f.id, payload);

    this.savingIncident = true;

    this.formError = null;

    request.subscribe({
      next: () => {
        this.savingIncident = false;

        this.closeForms();

        this.loadIncidents();
      },

      error: (e: HttpErrorResponse) => {
        this.savingIncident = false;

        this.formError = this.errorText(e);
      },
    });
  }

  deleteIncident(incident: BackendIncident): void {
    if (!this.puedeGestionarIncidencias) return;

    if (!confirm(`¿Eliminar la incidencia «${incident.titulo}»?`)) return;

    this.incidentService.deleteIncident(incident.id).subscribe({
      next: () => this.loadIncidents(),

      error: (e: HttpErrorResponse) => (this.formError = this.errorText(e)),
    });
  }

  canManageTasks(): boolean {
    const rol = this.usuario()?.rol;
    return rol === 'ADMIN' || rol === 'CUIDADOR';
  }

  taskToneClass(tipo: TaskType): string {
    return {
      ALIMENTACION: 'task-alimentacion',
      VISITA: 'task-visita',
      VETERINARIO: 'task-veterinario',
      REVISION: 'task-revision',
      OTRO: 'task-otro',
    }[tipo];
  }

  taskTypeLabel(tipo: TaskType): string {
    return this.tareaTipoLabels[tipo] ?? tipo;
  }

  taskStatusLabel(estado: TaskStatus): string {
    return this.tareaEstadoLabels[estado] ?? estado;
  }

  isSelectedDay(day: number): boolean {
    const candidateDate = new Date(
      this.taskCalendarMonth.getFullYear(),
      this.taskCalendarMonth.getMonth(),
      day,
    );
    return this.taskSelectedDate === this.localDateKey(candidateDate);
  }

  selectedCalendarDay(): number | null {
    const selected = this.selectedCalendarDate();
    return selected ? selected.getDate() : null;
  }

  private selectedCalendarDate(): Date | null {
    if (!this.taskSelectedDate) return null;
    const [year, month, day] = this.taskSelectedDate.split('-').map(Number);
    if (!year || !month || !day) return null;
    return new Date(year, month - 1, day);
  }

  selectCalendarDay(day: number): void {
    const selectedDate = new Date(
      this.taskCalendarMonth.getFullYear(),
      this.taskCalendarMonth.getMonth(),
      day,
    );
    this.taskSelectedDate = this.localDateKey(selectedDate);
  }

  previousCalendarMonth(): void {
    const nextMonth = new Date(
      this.taskCalendarMonth.getFullYear(),
      this.taskCalendarMonth.getMonth() - 1,
      1,
    );
    this.taskCalendarMonth = nextMonth;
    this.syncSelectedDateToCalendarMonth();
  }

  nextCalendarMonth(): void {
    const nextMonth = new Date(
      this.taskCalendarMonth.getFullYear(),
      this.taskCalendarMonth.getMonth() + 1,
      1,
    );
    this.taskCalendarMonth = nextMonth;
    this.syncSelectedDateToCalendarMonth();
  }

  goToCurrentMonth(): void {
    this.taskCalendarMonth = new Date(this.now.getFullYear(), this.now.getMonth(), 1);
    this.taskSelectedDate = this.localDateKey(this.now);
  }

  private syncSelectedDateToCalendarMonth(): void {
    const selected = this.selectedCalendarDate();
    if (!selected) {
      this.taskSelectedDate = this.localDateKey(this.taskCalendarMonth);
      return;
    }

    const monthDayLimit = new Date(
      this.taskCalendarMonth.getFullYear(),
      this.taskCalendarMonth.getMonth() + 1,
      0,
    ).getDate();
    const day = Math.min(selected.getDate(), monthDayLimit);
    const nextSelectedDate = new Date(
      this.taskCalendarMonth.getFullYear(),
      this.taskCalendarMonth.getMonth(),
      day,
    );
    this.taskSelectedDate = this.localDateKey(nextSelectedDate);
  }

  hasTasksOnDay(day: number): boolean {
    return this.tasksForDay(day).length > 0;
  }

  tasksForDay(day: number): BackendTarea[] {
    const calendarDay = new Date(
      this.taskCalendarMonth.getFullYear(),
      this.taskCalendarMonth.getMonth(),
      day,
    );
    const dayKey = this.localDateKey(calendarDay);
    return this.rawTasks().filter((task) => this.localDateKey(task.fechaHora) === dayKey);
  }

  get selectedDayLabel(): string {
    if (!this.taskSelectedDate) return 'Día';
    const selected = new Date(`${this.taskSelectedDate}T12:00:00`);
    return selected.toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' });
  }

  newTaskForSelectedDay(dateValue?: string): void {
    if (!this.canManageTasks()) {
      this.formError = 'No tienes permisos para gestionar tareas.';
      return;
    }

    const dayKey = dateValue ?? this.taskSelectedDate;
    const now = new Date();
    const selectedDate = dayKey
      ? new Date(
          `${dayKey}T${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`,
        )
      : new Date();

    this.taskForm = {
      id: null,
      titulo: '',
      descripcion: '',
      tipo: 'VISITA',
      estado: 'PENDIENTE',
      fechaHora: this.toDatetimeLocalValue(selectedDate),
      catId: null,
      colonyId: null,
    };

    this.taskFieldErrors = {};
    this.formError = null;
    this.taskFormOpen = true;
  }

  editTask(task: BackendTarea): void {
    if (!this.canManageTasks()) return;

    this.taskForm = {
      id: task.id,
      titulo: task.titulo,
      descripcion: task.descripcion ?? '',
      tipo: task.tipo,
      estado: task.estado,
      fechaHora: this.toDatetimeLocalValue(new Date(task.fechaHora)),
      catId: task.catId ?? null,
      colonyId: task.colonyId ?? null,
    };
    this.taskFieldErrors = {};
    this.formError = null;
    this.taskFormOpen = true;
  }

  saveTask(): void {
    if (this.savingTask) return;

    if (!this.canManageTasks()) {
      this.formError = 'No tienes permisos para gestionar tareas.';
      return;
    }

    const f = this.taskForm;
    const errors: Partial<Record<keyof TaskForm, string>> = {};

    if (!f.titulo.trim()) {
      errors.titulo = 'El título es obligatorio.';
    }

    if (!f.fechaHora) {
      errors.fechaHora = 'La fecha y hora son obligatorias.';
    }

    this.taskFieldErrors = errors;

    if (Object.keys(errors).length > 0) {
      this.formError = 'Revisa los datos de la tarea antes de guardar.';
      return;
    }

    const payload: TareaPayload = {
      titulo: f.titulo.trim(),
      descripcion: f.descripcion.trim() || null,
      tipo: f.tipo,
      estado: f.estado,
      fechaHora: this.normalizeDateTime(f.fechaHora),
      catId: f.catId,
      colonyId: f.colonyId,
    };

    const request =
      f.id === null
        ? this.tareaService.createTarea(payload)
        : this.tareaService.updateTarea(f.id, payload);

    this.savingTask = true;
    this.formError = null;

    request.subscribe({
      next: () => {
        this.savingTask = false;
        this.taskFormOpen = false;
        this.taskFieldErrors = {};
        this.loadTasks();
      },
      error: (e: HttpErrorResponse) => {
        this.savingTask = false;
        this.formError = this.errorText(e);
      },
    });
  }

  toggleTaskCompleted(task: BackendTarea): void {
    if (!this.canManageTasks()) {
      this.formError = 'No tienes permisos para gestionar tareas.';
      return;
    }

    this.tareaService.completarTarea(task.id, task.estado !== 'COMPLETADA').subscribe({
      next: () => this.loadTasks(),
      error: (e: HttpErrorResponse) => (this.formError = this.errorText(e)),
    });
  }

  deleteTask(task: BackendTarea): void {
    if (!this.canManageTasks()) return;

    if (!confirm(`¿Eliminar la tarea «${task.titulo}»?`)) return;

    this.tareaService.deleteTarea(task.id).subscribe({
      next: () => this.loadTasks(),
      error: (e: HttpErrorResponse) => (this.formError = this.errorText(e)),
    });
  }

  incidentTypeLabel(tipo: IncidentType): string {
    return this.incidentTypeLabels[tipo] ?? tipo;
  }

  incidentPriorityLabel(prioridad: IncidentPriority): string {
    return this.incidentPriorityLabels[prioridad] ?? prioridad;
  }

  incidentStatusLabel(estado: IncidentStatus): string {
    return this.incidentStatusLabels[estado] ?? estado;
  }

  // ---------- Carga ----------

  private loadColonies(): void {
    this.colonyService.getColonies().subscribe({
      next: (colonies) => this.rawColonies.set(colonies),

      error: () => this.rawColonies.set([]),
    });
  }

  private loadCats(): void {
    this.catService.getCats().subscribe({
      next: (cats) => this.rawCats.set(cats),

      error: () => this.rawCats.set([]),
    });
  }

  private loadIncidents(): void {
    this.incidentService.getIncidents().subscribe({
      next: (incidents) => this.rawIncidents.set(incidents),

      error: () => this.rawIncidents.set([]),
    });
  }

  private loadTasks(): void {
    this.tasksLoadError = null;

    this.tareaService.getTareas().subscribe({
      next: (tasks) => {
        this.rawTasks.set(tasks);
        this.tasksLoadError = null;
      },
      error: () => {
        this.rawTasks.set([]);
        this.tasksLoadError =
          'No se pudieron cargar las tareas. Comprueba la conexión con el servidor.';
      },
    });
  }

  private loadUsers(rol: string): void {
    if (rol !== 'ADMIN') {
      this.users.set([]);

      this.usersMessage = 'Solo la administración puede ver este listado.';

      return;
    }

    this.usersMessage = null;

    this.userService.getUsers().subscribe({
      next: (users) => this.users.set(users),

      error: () => {
        this.users.set([]);

        this.usersMessage = 'No se ha podido cargar el listado.';
      },
    });
  }

  // ---------- Utilidades ----------

  private errorText(e: HttpErrorResponse): string {
    if (e.status === 0) return 'No hay conexión con el servidor.';

    if (e.error?.mensaje) return e.error.mensaje;

    if (e.error?.campos) return Object.values(e.error.campos).join(' ');

    if (e.status === 403) return 'No tienes permisos para esta operación.';

    return 'No se ha podido completar la operación.';
  }

  private localDateKey(date: Date | string): string {
    const value = typeof date === 'string' ? new Date(date) : date;
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private toDatetimeLocalValue(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  private normalizeDateTime(value: string): string {
    if (!value) return value;
    return value.length === 16 ? `${value}:00` : value;
  }

  private emptyColonyForm(): ColonyForm {
    return { id: null, name: '', description: '', location: '', active: true };
  }

  private emptyCatForm(): CatForm {
    return {
      id: null,
      name: '',
      sex: 'UNKNOWN',
      birthDate: '',
      color: '',
      status: 'EN_COLONIA',

      neutered: false,
      identified: false,
      colonyId: null,
      observations: '',
    };
  }

private emptyUserForm(): UserForm {
  return {
    id: null,
    nombre: '',
    email: '',
    password: '',
    rol: 'CUIDADOR',
    activo: true
  };
}
  private emptyIncidentForm(): IncidentForm {
    return {
      id: null,

      titulo: '',

      descripcion: '',

      tipo: 'OTRO',

      prioridad: 'MEDIA',

      estado: 'ABIERTA',

      catId: null,

      colonyId: null,
    };
  }

  private emptyTaskForm(): TaskForm {
    return {
      id: null,
      titulo: '',
      descripcion: '',
      tipo: 'VISITA',
      estado: 'PENDIENTE',
      fechaHora: this.toDatetimeLocalValue(new Date()),
      catId: null,
      colonyId: null,
    };
  }

  private buildCalendar(year: number, month: number): (number | null)[][] {
    const offset = (new Date(year, month, 1).getDay() + 6) % 7;

    const total = new Date(year, month + 1, 0).getDate();

    const cells: (number | null)[] = [
      ...Array<null>(offset).fill(null),

      ...Array.from({ length: total }, (_, i) => i + 1),
    ];

    while (cells.length % 7 !== 0) cells.push(null);

    const weeks: (number | null)[][] = [];

    for (let i = 0; i < cells.length; i += 7) weeks.push(cells.slice(i, i + 7));

    return weeks;
  }

  get requiresCatColony(): boolean {
    return this.catForm.status === 'EN_COLONIA';
  }

  onCatStatusChange(): void {
    if (!this.requiresCatColony) {
      this.catColonyValidationError = false;
    }
  }

  onCatColonyChange(): void {
    if (this.catForm.colonyId !== null) {
      this.catColonyValidationError = false;
    }
  }
}
