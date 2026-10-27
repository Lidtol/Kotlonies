import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type Section = 'Inicio' | 'Colonias' | 'Gatos' | 'Voluntariado' | 'Calendario' | 'Perfil';

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
    { label: 'Gatos', icon: '♧' },
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
    }
  ];

  get filteredColonies(): Colony[] {
    const term = this.searchTerm.trim().toLocaleLowerCase('es');
    if (!term) return this.colonies;
    return this.colonies.filter(colony =>
      colony.name.toLocaleLowerCase('es').includes(term) ||
      colony.location.toLocaleLowerCase('es').includes(term)
    );
  }

  selectSection(section: Section): void {
    this.activeSection = section;
  }
}
