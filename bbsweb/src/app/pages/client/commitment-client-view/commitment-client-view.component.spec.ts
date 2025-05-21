import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommitmentClientViewComponent } from './commitment-client-view.component';

describe('CommitmentClientViewComponent', () => {
  let component: CommitmentClientViewComponent;
  let fixture: ComponentFixture<CommitmentClientViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommitmentClientViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CommitmentClientViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
