import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ColonyList } from './colony-list';

describe('ColonyList', () => {
  let component: ColonyList;
  let fixture: ComponentFixture<ColonyList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ColonyList],
    }).compileComponents();

    fixture = TestBed.createComponent(ColonyList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
