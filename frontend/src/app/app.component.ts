import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type CatFilter = 'Todos' | 'Esterilizados' | 'Pendientes';

type Section = 'Inicio' | 'Colonias' | 'Gatos' | 'Voluntariado' | 'Calendario' | 'Perfil';

interface Cat {
  name: string | null;
  code: string;
  colony: string;
  trait: string;
  sterilized: boolean;
  lastVet: string;
}

interface Colony {
  name: string;
  location: string;
  cats: number;
  volunteers: number;
  status: 'Activa' | 'Pendiente';
  image: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  activeSection: Section = 'Inicio';
  searchTerm = '';

  readonly navigation: { label: Section; icon: string }[] = [
    { label: 'Inicio', icon: '⌂' },
    { label: 'Colonias', icon: '♧' },
    { label: 'Gatos', icon: '♢' },
    { label: 'Voluntariado', icon: '♙' },
    { label: 'Calendario', icon: '▦' },
    { label: 'Perfil', icon: '◉' }
  ];

  readonly stats = [
    { label: 'Colonias activas', value: '12', icon: '⌂', tone: 'violet' },
    { label: 'Gatos registrados', value: '86', icon: '♧', tone: 'pink' },
    { label: 'Personas voluntarias', value: '24', icon: '♙', tone: 'blue' },
    { label: 'En adopción', value: '3', icon: '♡', tone: 'peach' }
  ];

  readonly colonies: Colony[] = [
    {
      name: 'Colonia Centro',
      location: 'Valencia',
      cats: 12,
      volunteers: 2,
      status: 'Activa',
      image: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=900&q=85'
    },
    {
      name: 'Colonia Playa',
      location: 'Valencia',
      cats: 8,
      volunteers: 1,
      status: 'Activa',
      image: 'https://images.unsplash.com/photo-1495360010541-f48722b34f7d?auto=format&fit=crop&w=900&q=85'
    },
    {
      name: 'Colonia Universidad',
      location: 'Valencia',
      cats: 15,
      volunteers: 4,
      status: 'Activa',
      image: 'https://images.unsplash.com/photo-1511044568932-338cba0ad803?auto=format&fit=crop&w=900&q=85'
    },
    {
      name: 'Colonia Jardín del Turia',
      location: 'Valencia',
      cats: 9,
      volunteers: 3,
      status: 'Activa',
      image: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=900&q=85'
    },
    {
      name: 'Colonia Puerto',
      location: 'Valencia',
      cats: 6,
      volunteers: 0,
      status: 'Pendiente',
      image: 'https://images.unsplash.com/photo-1495360010541-f48722b34f7d?auto=format&fit=crop&w=900&q=85'
    }
  ];

  readonly subtitles: Record<Section, string> = {
    Inicio: 'Esto es lo que está pasando hoy en tus colonias.',
    Colonias: 'Todas las colonias que gestionas y su estado.',
    Gatos: 'Fichas de los gatos, incluidos los que aún no tienen nombre.',
    Voluntariado: 'Las personas que cuidan de las colonias y su rol.',
    Calendario: 'Visitas, alimentación y revisiones previstas.',
    Perfil: 'Tus datos y los permisos de cada rol.'
  };

  // Gatos
  catFilter: CatFilter = 'Todos';
  readonly catFilters: CatFilter[] = ['Todos', 'Esterilizados', 'Pendientes'];
  readonly cats: Cat[] = [
    { name: 'Luna', code: 'C-001', colony: 'Colonia Centro', trait: 'Atigrada gris, muesca en la oreja izquierda', sterilized: true, lastVet: '12 oct 2026' },
    { name: 'Miso', code: 'C-002', colony: 'Colonia Centro', trait: 'Naranja, pelo corto', sterilized: true, lastVet: '03 sep 2026' },
    { name: null, code: 'C-014', colony: 'Colonia Playa', trait: 'Negro con mancha blanca en el pecho', sterilized: false, lastVet: 'Sin registrar' },
    { name: 'Nube', code: 'C-021', colony: 'Colonia Universidad', trait: 'Blanca, ojos azules', sterilized: false, lastVet: '20 oct 2026' },
    { name: 'Tigre', code: 'C-022', colony: 'Colonia Universidad', trait: 'Atigrado marrón, cola corta', sterilized: true, lastVet: '28 jul 2026' },
    { name: null, code: 'C-031', colony: 'Colonia Jardín del Turia', trait: 'Carey, pelo largo', sterilized: false, lastVet: 'Sin registrar' }
  ];

  // Voluntariado
  readonly volunteers = [
    { name: 'Lidia', role: 'Administradora', roleClass: 'admin', colony: 'Todas las colonias' },
    { name: 'Ana García', role: 'Cuidadora', roleClass: '', colony: 'Colonia Centro' },
    { name: 'Marcos Ruiz', role: 'Cuidador', roleClass: '', colony: 'Colonia Playa' },
    { name: 'Paula Soler', role: 'Cuidadora', roleClass: '', colony: 'Colonia Universidad' },
    { name: 'Invitado/a', role: 'Consulta', roleClass: 'viewer', colony: 'Solo lectura' }
  ];

  // Calendario (octubre de 2026, datos de demostración)
  readonly calendarMonth = 'Octubre 2026';
  readonly todayDay = 27;
  readonly weekDays = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
  readonly events = [
    { day: 6, title: 'Alimentación', colony: 'Colonia Playa', tone: 'pink' },
    { day: 14, title: 'Limpieza de comederos', colony: 'Colonia Centro', tone: 'green' },
    { day: 20, title: 'Revisión veterinaria', colony: 'Colonia Universidad', tone: 'violet' },
    { day: 27, title: 'Alimentación', colony: 'Colonia Centro', tone: 'pink' },
    { day: 29, title: 'Revisión veterinaria', colony: 'Colonia Playa', tone: 'violet' },
    { day: 31, title: 'Limpieza de comederos', colony: 'Colonia Universidad', tone: 'green' }
  ];
  readonly upcomingEvents = this.events.filter(e => e.day >= this.todayDay);
  readonly calendarWeeks: (number | null)[][] = this.buildCalendar(2026, 9);

  // Perfil (roles propuestos, pendientes de definir en la fase de diseño)
  readonly roles = [
    { name: 'Administradora', description: 'Gestiona colonias, gatos, personas y ajustes.' },
    { name: 'Cuidador/a', description: 'Registra cuidados, tareas e incidencias de sus colonias.' },
    { name: 'Consulta', description: 'Solo puede consultar la información.' }
  ];

  get filteredColonies(): Colony[] {
    const term = this.searchTerm.trim().toLocaleLowerCase('es');
    if (!term) return this.colonies;
    return this.colonies.filter(colony =>
      colony.name.toLocaleLowerCase('es').includes(term) ||
      colony.location.toLocaleLowerCase('es').includes(term)
    );
  }

  get filteredCats(): Cat[] {
    const term = this.searchTerm.trim().toLocaleLowerCase('es');
    return this.cats.filter(cat => {
      const matchesFilter = this.catFilter === 'Todos' || (this.catFilter === 'Esterilizados') === cat.sterilized;
      const text = `${cat.name ?? 'Sin nombre'} ${cat.code} ${cat.colony}`.toLocaleLowerCase('es');
      return matchesFilter && (!term || text.includes(term));
    });
  }

  eventsOn(day: number) {
    return this.events.filter(event => event.day === day);
  }

  private buildCalendar(year: number, month: number): (number | null)[][] {
    const offset = (new Date(year, month, 1).getDay() + 6) % 7; // semana desde el lunes
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

  selectSection(section: Section): void {
    this.activeSection = section;
    this.searchTerm = '';
    this.catFilter = 'Todos';
  }
}