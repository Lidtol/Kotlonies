import { Routes } from '@angular/router';
import { ColonyList } from './features/colonies/colony-list/colony-list';

export const routes: Routes = [
  { path: '', redirectTo: 'colonies', pathMatch: 'full' },
  { path: 'colonies', component: ColonyList, title: 'Colonias' },
];