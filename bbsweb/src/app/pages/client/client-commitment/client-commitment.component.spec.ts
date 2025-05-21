import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientCommitmentComponent } from './client-commitment.component';

describe('ClientCommitmentComponent', () => {
  let component: ClientCommitmentComponent;
  let fixture: ComponentFixture<ClientCommitmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClientCommitmentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClientCommitmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
