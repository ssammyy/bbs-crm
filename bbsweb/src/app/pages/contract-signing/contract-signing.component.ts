import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormArray, AbstractControl } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { InputNumberModule } from 'primeng/inputnumber';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { FileUploadModule } from 'primeng/fileupload';
import { InputTextModule } from 'primeng/inputtext';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ToastModule } from 'primeng/toast';
import { CalendarModule } from 'primeng/calendar';
import { InvoiceService } from '../invoice-form/invoice.service';
import { ContractService } from './contract.service';
import { UploadService } from '../service/upload.service';

interface PaymentSchedule {
  description: string;
  percentage: number;
  amount: number;
  dueDate: Date | null;
}

@Component({
  selector: 'app-contract-signing',
  templateUrl: './contract-signing.component.html',
  styleUrls: ['./contract-signing.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    InputNumberModule,
    ButtonModule,
    CardModule,
    FileUploadModule,
    InputTextModule,
    ReactiveFormsModule,
    ToastModule,
    CalendarModule
  ]
})
export class ContractSigningComponent implements OnInit {
  @Input() boqAmount: number = 0;
  @Input() clientId: number = 0;
  @Output() contractSigned = new EventEmitter<void>();

  contractForm: FormGroup;
  uploadedFile: File | null = null;
  totalPercentage: number = 0;
  dueDateErrors: boolean[] = [];
  contractResult: any = null;
  loadingSubmit = false;
  isEditing = false;
  contractId: number | null = null;
    loading = false;

  constructor(
    private fb: FormBuilder,
    private messageService: MessageService,
    private invoiceService: InvoiceService,
    private contractService: ContractService,
    private uploadService: UploadService
  ) {
    this.contractForm = this.fb.group({
      contractFile: [null, Validators.required],
      projectStartDate: [null, Validators.required],
      projectDuration: [120, [Validators.required, Validators.min(1)]],
      paymentSchedules: this.fb.array([])
    });
  }

  ngOnInit() {
    // Check for existing contract
    if (this.clientId) {
      this.contractService.getContractByClientId(this.clientId).subscribe({
        next: (result) => {
          if (result && result.contract && result.contract.id) {
            this.contractId = result.contract.id;
            this.isEditing = false;
            this.patchContractForm(result.contract);
            // Set contractResult with invoices for the template
            this.contractResult = {
              ...result.contract,
              mainInvoice: result.mainInvoice,
              installmentInvoices: result.installmentInvoices || []
            };
          }
        },
        error: () => {
          // No contract found, show form
        }
      });
    }
    this.addPaymentSchedule();
    this.contractForm.get('projectStartDate')?.valueChanges.subscribe(() => this.validateDueDates());
    this.contractForm.get('projectDuration')?.valueChanges.subscribe(() => this.validateDueDates());
    this.paymentSchedules.valueChanges.subscribe(() => this.validateDueDates());
  }

  patchContractForm(contract: any) {
    this.contractForm.patchValue({
      projectStartDate: contract.projectStartDate,
      projectDuration: contract.projectDuration,
      // Add more fields as needed
    });
    // Patch installments if needed
  }

  startEdit() {
    this.isEditing = true;
    // Optionally, patch form with contractResult
  }

  async saveEdit() {
    if (this.contractId && this.contractForm.valid) {
      let contractFileUrl = this.contractResult?.contractFileUrl || null;
      if (this.uploadedFile) {
        try {
          const uploadResp: any = await this.uploadService.upload('CONTRACT_DOCUMENT', this.uploadedFile, this.clientId).toPromise();
          contractFileUrl = uploadResp.fileUrl || uploadResp.url || null;
        } catch (e) {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to upload contract document' });
          return;
        }
      }
      const updated = { ...this.contractForm.value, id: this.contractId, contractFileUrl };
      this.contractService.updateContract(this.contractId, updated).subscribe({
        next: (result) => {
          this.contractResult = result;
          this.isEditing = false;
          this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Contract updated' });
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to update contract' });
        }
      });
    }
  }

  get paymentSchedules() {
    return this.contractForm.get('paymentSchedules') as FormArray;
  }

  createPaymentSchedule(): FormGroup {
    return this.fb.group({
      description: ['', Validators.required],
      percentage: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
      amount: [0],
      dueDate: [null, Validators.required]
    });
  }

  addPaymentSchedule() {
    const schedule = this.createPaymentSchedule();
    this.paymentSchedules.push(schedule);
    this.updateAmounts();
    this.validateDueDates();
  }

  removePaymentSchedule(index: number) {
    this.paymentSchedules.removeAt(index);
    this.updateAmounts();
    this.validateDueDates();
  }

  onFileSelect(event: any) {
    this.uploadedFile = event.files[0];
    this.contractForm.patchValue({
      contractFile: this.uploadedFile
    });
  }

  updateAmounts() {
    this.totalPercentage = 0;
    this.paymentSchedules.controls.forEach(control => {
      const percentage = control.get('percentage')?.value || 0;
      this.totalPercentage += percentage;
      const amount = (this.boqAmount * percentage) / 100;
      control.patchValue({ amount: amount }, { emitEvent: false });
    });
  }

  get projectEndDate(): Date | null {
    const start = this.contractForm.get('projectStartDate')?.value;
    const duration = this.contractForm.get('projectDuration')?.value;
    if (start && duration) {
      const end = new Date(start);
      end.setDate(end.getDate() + Number(duration));
      return end;
    }
    return null;
  }

  validateDueDates() {
    const endDate = this.projectEndDate;
    this.dueDateErrors = this.paymentSchedules.controls.map((control: AbstractControl) => {
      const dueDate = control.get('dueDate')?.value;
      if (dueDate && endDate) {
        return new Date(dueDate).getTime() > endDate.getTime();
      }
      return false;
    });
    // Set form error if any due date is invalid
    if (this.dueDateErrors.some(e => e)) {
      this.contractForm.setErrors({ dueDateInvalid: true });
    } else {
      this.contractForm.setErrors(null);
    }
  }

  async onSubmit() {
    this.validateDueDates();
    if (this.contractForm.valid && this.totalPercentage === 100) {
      this.loadingSubmit = true;
      const formValue = this.contractForm.value;
      let contractFileUrl = null;
      if (this.uploadedFile) {
        try {
          const uploadResp: any = await this.uploadService.upload('CONTRACT_DOCUMENT', this.uploadedFile, this.clientId).toPromise();
          contractFileUrl = uploadResp.fileUrl || uploadResp.url || null;
        } catch (e) {
          this.loadingSubmit = false;
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to upload contract document' });
          return;
        }
      }
      const contractData = {
        clientId: this.clientId,
        projectName: '', // You can set this from another input or prop
        boqAmount: this.boqAmount,
        projectStartDate: formValue.projectStartDate,
        projectDuration: formValue.projectDuration,
        installments: formValue.paymentSchedules.map((s: any) => ({
          description: s.description,
          percentage: s.percentage,
          amount: s.amount,
          dueDate: s.dueDate
        })),
        contractFileUrl
      };
      this.contractService.createContract(contractData).subscribe({
        next: (result) => {
          // Set contractResult with installments for the template
          this.contractResult = {
            ...result.contract,
            mainInvoice: result.mainInvoice,
            installmentInvoices: result.installmentInvoices || []
          };
          this.loadingSubmit = false;
          this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Contract and document uploaded successfully' });
        },
        error: (err) => {
          this.loadingSubmit = false;
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to create contract/invoices' });
        }
      });
    } else if (this.dueDateErrors.some(e => e)) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'One or more installment due dates exceed the project end date.' });
    } else {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Please ensure all fields are filled and percentages total 100%' });
    }
  }

  isTotalValid(): boolean {
    return this.totalPercentage === 100;
  }

  get nextPaymentDue(): string {
    if (!this.contractResult?.installmentInvoices?.length) return '';
    const next = this.contractResult.installmentInvoices.find((i: any) => !i.cleared);
    return next ? next.dateIssued : 'All Paid';
  }

  // --- Progress Bar Logic ---
  get contractStartDate(): Date | null {
    if (this.contractResult?.projectStartDate) {
      return new Date(this.contractResult.projectStartDate);
    }
    return null;
  }

  get contractDuration(): number {
    return this.contractResult?.projectDuration || 0;
  }

  get contractEndDate(): Date | null {
    if (this.contractStartDate && this.contractDuration) {
      const end = new Date(this.contractStartDate);
      end.setDate(end.getDate() + Number(this.contractDuration));
      return end;
    }
    return null;
  }

  get daysElapsed(): number {
    if (!this.contractStartDate) return 0;
    const now = new Date();
    const diff = Math.floor((now.getTime() - this.contractStartDate.getTime()) / (1000 * 60 * 60 * 24));
    return Math.max(0, Math.min(diff, this.contractDuration));
  }

  get daysRemaining(): number {
    if (!this.contractStartDate || !this.contractDuration) return 0;
    return Math.max(0, this.contractDuration - this.daysElapsed);
  }

  get progressPercent(): number {
    if (!this.contractDuration) return 0;
    return Math.min(100, Math.round((this.daysElapsed / this.contractDuration) * 100));
  }

  get totalAmountPaid(): number {
    if (!this.contractResult?.installmentInvoices) return 0;
    return this.contractResult.installmentInvoices
      .filter((inv: any) => inv.cleared)
      .reduce((sum: number, inv: any) => sum + (inv.total || 0), 0);
  }
}
