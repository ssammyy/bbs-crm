import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContractSigningComponent } from './contract-signing.component';

describe('ContractSigningComponent', () => {
  let component: ContractSigningComponent;
  let fixture: ComponentFixture<ContractSigningComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContractSigningComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ContractSigningComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
