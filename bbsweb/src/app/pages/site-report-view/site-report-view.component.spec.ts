import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SiteReportViewComponent } from './site-report-view.component';

describe('SiteReportViewComponent', () => {
  let component: SiteReportViewComponent;
  let fixture: ComponentFixture<SiteReportViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SiteReportViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SiteReportViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
