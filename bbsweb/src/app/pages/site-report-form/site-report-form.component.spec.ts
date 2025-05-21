import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SiteReportFormComponent } from './site-report-form.component';

describe('SiteReportFormComponent', () => {
  let component: SiteReportFormComponent;
  let fixture: ComponentFixture<SiteReportFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SiteReportFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SiteReportFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
