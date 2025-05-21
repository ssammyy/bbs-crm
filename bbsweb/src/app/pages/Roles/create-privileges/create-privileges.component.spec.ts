import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreatePrivilegesComponent } from './create-privileges.component';

describe('CreatePrivilegesComponent', () => {
  let component: CreatePrivilegesComponent;
  let fixture: ComponentFixture<CreatePrivilegesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreatePrivilegesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreatePrivilegesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
