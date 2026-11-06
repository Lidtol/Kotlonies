import { Component, computed, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { LoginComponent } from './features/login/login.component';
import { AuthService } from './core/auth.service';
import { BackendColony, ColonyService } from './core/colony.service';
import { BackendCat, CatPayload, CatService } from './core/cat.service';
import { BackendUser, UserPayload, UserService } from './core/user.service';

type CatFilter = 'Todos' | 'Esterilizados' | 'Pendientes';
type Section = 'Inicio' | 'Colonias' | 'Gatos' | 'Voluntariado' | 'Calendario' | 'Perfil';
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
  nombre: string;
  email: string;
  password: string;
  rol: string;
  activo: boolean;
}

const PLACEHOLDER_IMAGE =
  'https://images.unsplash.com/photo-1573865526739-10659fec78a5?auto=format&fit=crop&w=600&q=80';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, LoginComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  private readonly auth = inject(AuthService);
  private readonly colonyService = inject(ColonyService);
  private readonly catService = inject(CatService);
  private readonly userService = inject(UserService);

  readonly usuario = this.auth.usuario;
  readonly cargandoSesion = this.auth.cargando;

  activeView: View = 'Login';
  searchTerm = '';

  private readonly rawColonies = signal<BackendColony[]>([]);
  private readonly rawCats = signal<BackendCat[]>([]);
  users: BackendUser[] = [];
  usersMessage: string | null = null;

  // Datos derivados como signals: los arrays base deben ser signals también; si son
  // propiedades corrientes, el computed no se entera de su cambio y mostraría siempre 0.
  private readonly colonyCountSignal = computed(() => this.rawColonies().length);
  private readonly activeColonyCountSignal = computed(() => this.rawColonies().filter(c => c.active).length);
  private readonly catCountSignal = computed(() => this.rawCats().length);
  private readonly adoptionCountSignal = computed(() => this.rawCats().filter(c => c.status === 'EN_ADOPCION').length);
  private readonly userCountSignal = computed(() => this.users.length);

  // Formularios
  colonyFormOpen = false;
  catFormOpen = false;
  userFormOpen = false;
  formError: string | null = null;
  colonyForm: ColonyForm = this.emptyColonyForm();
  catForm: CatForm = this.emptyCatForm();
  userForm: UserForm = this.emptyUserForm();

  // Guardas de envío: evitan que varios clics dispare varias peticiones y duplique el registro.
  savingColony = false;
  savingCat = false;
  savingUser = false;

  // Todas las entradas del menú; se filtran según el rol al mostrarlas.
  private readonly allNavigation: { label: Section; icon: string; adminOnly?: boolean }[] = [
    { label: 'Inicio', icon: '⌂' },
    { label: 'Colonias', icon: '♧' },
    { label: 'Gatos', icon: '♢' },
    { label: 'Voluntariado', icon: '♙', adminOnly: true },
    { label: 'Calendario', icon: '▦' },
    { label: 'Perfil', icon: '◉' }
  ];

  /**
   * Entries shown in the left menu. "Voluntariado" is only for ADMIN; the endpoint
   * behind it is already restricted in the backend, so the menu stays coherent with it.
   * It is a getter because the template uses *ngFor, which does not unwrap signals.
   */
  get navigation(): { label: Section; icon: string }[] {
    return this.allNavigation
      .filter(item => !item.adminOnly || this.esAdministrador())
      .map(item => ({ label: item.label, icon: item.icon }));
  }

  esAdministrador(): boolean {
    return this.usuario()?.rol === 'ADMIN';
  }

  readonly subtitles: Record<Section, string> = {
    Inicio: 'Esto es lo que está pasando hoy en tus colonias.',
    Colonias: 'Todas las colonias que gestionas y su estado.',
    Gatos: 'Fichas de los gatos, incluidos los que aún no tienen nombre.',
    Voluntariado: 'Las personas con acceso a la plataforma y su rol.',
    Calendario: 'Visitas, alimentación y revisiones previstas.',
    Perfil: 'Tus datos y los permisos de cada rol.'
  };

  readonly catFilters: CatFilter[] = ['Todos', 'Esterilizados', 'Pendientes'];
  catFilter: CatFilter = 'Todos';

  readonly statusLabels: Record<string, string> = {
    EN_COLONIA: 'En colonia',
    EN_SEGUIMIENTO: 'En seguimiento',
    EN_ADOPCION: 'En adopción'
  };
  readonly statusOptions = Object.keys(this.statusLabels);
  readonly sexOptions = [
    { value: 'FEMALE', label: 'Hembra' },
    { value: 'MALE', label: 'Macho' },
    { value: 'UNKNOWN', label: 'Desconocido' }
  ];

  readonly roleLabels: Record<string, string> = {
    ADMIN: 'Administración',
    CUIDADOR: 'Cuidador/a',
    CONSULTA: 'Consulta'
  };
  readonly rolOptions = [
    { value: 'CUIDADOR', label: 'Cuidador/a' },
    { value: 'CONSULTA', label: 'Consulta' },
    { value: 'ADMIN', label: 'Administración' }
  ];
  readonly roles = [
    { name: 'Administración', description: 'Gestiona usuarios, colonias y gatos.' },
    { name: 'Cuidador/a', description: 'Registra y actualiza colonias y gatos.' },
    { name: 'Consulta', description: 'Solo puede consultar la información.' }
  ];

  // Calendario (sin backend todavía: no hay tareas)
  private readonly now = new Date();
  readonly todayLabel = this.now.toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' });
  readonly calendarMonth = this.now.toLocaleDateString('es-ES', { month: 'long', year: 'numeric' });
  readonly todayDay = this.now.getDate();
  readonly weekDays = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
  readonly events: { day: number; title: string; colony: string; tone: string }[] = [];
  readonly upcomingEvents = this.events.filter(e => e.day >= this.todayDay);
  readonly calendarWeeks = this.buildCalendar(this.now.getFullYear(), this.now.getMonth());

  constructor() {
    // Reacciona a la sesión: tras el login o al recuperarla al recargar la página.
    effect(() => {
      // No decidimos vista hasta saber si había sesión abierta en el backend.
      if (this.auth.cargando()) return;
      const user = this.auth.usuario();
      if (user) {
        if (this.activeView === 'Login') this.activeView = 'Inicio';
        this.loadColonies();
        this.loadCats();
        this.loadUsers(user.rol);
      } else {
        this.activeView = 'Login';
        this.rawColonies.set([]);
        this.rawCats.set([]);
        this.users = [];
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
      { label: 'Colonias activas', value: String(this.activeColonyCountSignal()), icon: '⌂', tone: 'violet' },
      { label: 'Gatos registrados', value: String(this.catCountSignal()), icon: '♧', tone: 'pink' },
      { label: 'Personas con acceso', value: String(this.userCountSignal()), icon: '♙', tone: 'blue' },
      { label: 'En adopción', value: String(this.adoptionCountSignal()), icon: '♡', tone: 'peach' }
    ];
  }

  get filteredColonies(): ColonyView[] {
    const term = this.searchTerm.trim().toLocaleLowerCase('es');
    return this.rawColonies()
      .map(c => ({
        id: c.id,
        name: c.name,
        location: c.location,
        active: c.active,
        cats: this.rawCats().filter(cat => cat.colonyId === c.id).length,
        image: PLACEHOLDER_IMAGE
      }))
      .filter(c => !term || c.name.toLocaleLowerCase('es').includes(term) || c.location.toLocaleLowerCase('es').includes(term));
  }

  get colonyOptions(): { id: number; name: string }[] {
    return this.rawColonies().filter(c => c.active).map(c => ({ id: c.id, name: c.name }));
  }

  get filteredCats(): CatView[] {
    const term = this.searchTerm.trim().toLocaleLowerCase('es');
    return this.rawCats()
      .map<CatView>(cat => ({
        id: cat.id,
        name: cat.name,
        code: '#' + String(cat.id).padStart(3, '0'),
        colony: cat.colonyName ?? 'Sin asignar',
        color: cat.color ?? '—',
        status: this.statusLabels[cat.status] ?? cat.status,
        sterilized: !!cat.neutered
      }))
      .filter(cat => {
        const matchesFilter = this.catFilter === 'Todos' || (this.catFilter === 'Esterilizados') === cat.sterilized;
        const text = `${cat.name ?? 'Sin nombre'} ${cat.code} ${cat.colony}`.toLocaleLowerCase('es');
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
    return this.savingUser ? 'Creando acceso...' : 'Guardar acceso';
  }

  roleLabel(rol: string | undefined): string {
    return rol ? (this.roleLabels[rol] ?? rol) : '';
  }

  roleClass(rol: string): string {
    return rol === 'ADMIN' ? 'admin' : rol === 'CONSULTA' ? 'viewer' : '';
  }

  eventsOn(day: number) {
    return this.events.filter(event => event.day === day);
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
    const raw = this.rawColonies().find(c => c.id === colony.id);
    if (!raw) return;
    this.closeForms();
    this.colonyForm = {
      id: raw.id,
      name: raw.name,
      description: raw.description ?? '',
      location: raw.location,
      active: raw.active
    };
    this.colonyFormOpen = true;
  }

  saveColony(): void {
    // Si ya hay una petición en marcha, se ignora el clic para no crear duplicados.
    if (this.savingColony) return;
    const f = this.colonyForm;
    const payload = {
      name: f.name.trim(),
      description: f.description.trim() || null,
      location: f.location.trim(),
      active: f.active
    };
    const request = f.id === null
      ? this.colonyService.createColony(payload)
      : this.colonyService.updateColony(f.id, payload);

    this.savingColony = true;
    this.formError = null;
    request.subscribe({
      next: () => { this.savingColony = false; this.closeForms(); this.loadColonies(); },
      error: (e: HttpErrorResponse) => {
        this.savingColony = false;
        this.formError = this.errorText(e);
      }
    });
  }

  archiveColony(colony: ColonyView): void {
    this.colonyService.archiveColony(colony.id).subscribe({
      next: () => this.loadColonies(),
      error: (e: HttpErrorResponse) => this.formError = this.errorText(e)
    });
  }

  // ---------- Gatos ----------
  newCat(): void {
    this.selectSection('Gatos');
    this.catForm = this.emptyCatForm();
    this.catFormOpen = true;
  }

  editCat(cat: CatView): void {
    const raw = this.rawCats().find(c => c.id === cat.id);
    if (!raw) return;
    this.closeForms();
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
      observations: raw.observations ?? ''
    };
    this.catFormOpen = true;
  }

  saveCat(): void {
    if (this.savingCat) return;
    const f = this.catForm;
    const payload: CatPayload = {
      name: f.name.trim() || null,
      sex: f.sex,
      birthDate: f.birthDate || null,
      color: f.color.trim() || null,
      status: f.status,
      neutered: f.neutered,
      identified: f.identified,
      colonyId: f.colonyId,
      observations: f.observations.trim() || null
    };
    const request = f.id === null
      ? this.catService.createCat(payload)
      : this.catService.updateCat(f.id, payload);

    this.savingCat = true;
    this.formError = null;
    request.subscribe({
      next: () => { this.savingCat = false; this.closeForms(); this.loadCats(); },
      error: (e: HttpErrorResponse) => {
        this.savingCat = false;
        this.formError = this.errorText(e);
      }
    });
  }

  deleteCat(cat: CatView): void {
    if (!confirm(`¿Eliminar a ${cat.name ?? 'este gato'}?`)) return;
    this.catService.deleteCat(cat.id).subscribe({
      next: () => this.loadCats(),
      error: (e: HttpErrorResponse) => this.formError = this.errorText(e)
    });
  }

  closeForms(): void {
    this.colonyFormOpen = false;
    this.catFormOpen = false;
    this.userFormOpen = false;
    this.formError = null;
  }

  // ---------- Voluntariado ----------
  newUser(): void {
    this.selectSection('Voluntariado');
    this.userForm = this.emptyUserForm();
    this.userFormOpen = true;
  }

  saveUser(): void {
    if (this.savingUser) return;
    if (this.usuario()?.rol !== 'ADMIN') {
      this.formError = 'Solo la administración puede crear cuentas.';
      return;
    }
    const f = this.userForm;
    const payload: UserPayload = {
      nombre: f.nombre.trim(),
      email: f.email.trim(),
      password: f.password,
      rol: f.rol,
      activo: f.activo
    };
    this.savingUser = true;
    this.formError = null;
    this.userService.createUser(payload).subscribe({
      next: () => { this.savingUser = false; this.closeForms(); this.loadUsers(this.usuario()!.rol); },
      error: (e: HttpErrorResponse) => {
        this.savingUser = false;
        this.formError = this.errorText(e);
      }
    });
  }

  // ---------- Carga ----------
  private loadColonies(): void {
    this.colonyService.getColonies().subscribe({
      next: colonies => this.rawColonies.set(colonies),
      error: () => this.rawColonies.set([])
    });
  }

  private loadCats(): void {
    this.catService.getCats().subscribe({
      next: cats => this.rawCats.set(cats),
      error: () => this.rawCats.set([])
    });
  }

  private loadUsers(rol: string): void {
    if (rol !== 'ADMIN') {
      this.users = [];
      this.usersMessage = 'Solo la administración puede ver este listado.';
      return;
    }
    this.usersMessage = null;
    this.userService.getUsers().subscribe({
      next: users => this.users = users,
      error: () => { this.users = []; this.usersMessage = 'No se ha podido cargar el listado.'; }
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

  private emptyColonyForm(): ColonyForm {
    return { id: null, name: '', description: '', location: '', active: true };
  }

  private emptyCatForm(): CatForm {
    return {
      id: null, name: '', sex: 'UNKNOWN', birthDate: '', color: '', status: 'EN_COLONIA',
      neutered: false, identified: false, colonyId: null, observations: ''
    };
  }

  private emptyUserForm(): UserForm {
    return { nombre: '', email: '', password: '', rol: 'CUIDADOR', activo: true };
  }

  private buildCalendar(year: number, month: number): (number | null)[][] {
    const offset = (new Date(year, month, 1).getDay() + 6) % 7;
    const total = new Date(year, month + 1, 0).getDate();
    const cells: (number | null)[] = [
      ...Array<null>(offset).fill(null),
      ...Array.from({ length: total }, (_, i) => i + 1)
    ];
    while (cells.length % 7 !== 0) cells.push(null);
    const weeks: (number | null)[][] = [];
    for (let i = 0; i < cells.length; i += 7) weeks.push(cells.slice(i, i + 7));
    return weeks;
  }
}