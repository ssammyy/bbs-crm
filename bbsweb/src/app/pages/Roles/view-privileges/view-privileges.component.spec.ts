import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewPrivilegesComponent } from './view-privileges.component';

describe('ViewPrivilegesComponent', () => {
  let component: ViewPrivilegesComponent;
  let fixture: ComponentFixture<ViewPrivilegesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewPrivilegesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewPrivilegesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
