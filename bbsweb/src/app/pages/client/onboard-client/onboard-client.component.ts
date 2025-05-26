import { Component, OnInit, HostListener } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf, NgSwitch, NgSwitchCase } from '@angular/common';
import { Steps } from 'primeng/steps';
import { MenuItem } from 'primeng/api';
import { InputText } from 'primeng/inputtext';
import { Calendar } from 'primeng/calendar';
import { Button } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { ClientDetailsService } from '../client.service';
import { MessagesService } from '../../../layout/service/messages.service';
import { Toast } from 'primeng/toast';
import { MultiSelectModule } from 'primeng/multiselect';
import { kenyanCounties } from '../../data/kenyan-counties';
import { allCountries } from '../../data/all-countries';
import { countryCodes } from '../../data/country-codes';
import { InputTextarea } from 'primeng/inputtextarea';
import { UserService } from '../../users/user.service';
import { User, UserGlobalService } from '../../service/user.service';
import { lenderInstitutions } from '../../data/lenders';

@Component({
    selector: 'app-onboard-client',
    imports: [ReactiveFormsModule, NgSwitch, Steps, NgSwitchCase, InputText, Calendar, Button, FormsModule, NgIf, DropdownModule, Toast, MultiSelectModule, InputTextarea],
    templateUrl: './onboard-client.component.html',
    styleUrl: './onboard-client.component.scss'
})
export class OnboardClientComponent implements OnInit {
    steps!: MenuItem[];
    activeIndex: number = 0;
    personalInfoForm!: FormGroup;
    idNumber: number = 0;
    loading = false;
    minDate: Date = new Date();
    agents: { label: string; value: number }[] = [];
    productOfferings = [
        { label: 'Jenga Kwako', value: 'JENGA_KWAKO' },
        { label: 'Jenga Stress Free', value: 'JENGA_STRESS_FREE' },
        { label: 'Labour Only Contracting', value: 'LABOUR_ONLY_CONTRACTING' },
        { label: 'Diaspora Building Solutions', value: 'DIASPORA_BUILDING_SOLUTIONS' },
        { label: 'Building Consultancy', value: 'BUILDING_CONSULTANCY' },
        { label: 'Renovations, Remodeling & Repairs', value: 'RENOVATIONS_REMODELING_REPAIRS' }
    ];
    productTags: { [key: string]: { label: string; value: string }[] } = {
        JENGA_KWAKO: [{ label: 'Jenga Kwako', value: 'JENGA_KWAKO' }],
        JENGA_STRESS_FREE: [
            { label: 'Commercial', value: 'JENGA_STRESS_FREE_COMMERCIAL' },
            { label: 'Residential', value: 'JENGA_STRESS_FREE_RESIDENTIAL' }
        ],
        LABOUR_ONLY_CONTRACTING: [
            { label: 'Commercial', value: 'LABOUR_ONLY_COMMERCIAL' },
            { label: 'Residential', value: 'LABOUR_ONLY_RESIDENTIAL' }
        ],
        DIASPORA_BUILDING_SOLUTIONS: [
            { label: 'Commercial', value: 'DIASPORA_COMMERCIAL' },
            { label: 'Residential', value: 'DIASPORA_RESIDENTIAL' }
        ],
        BUILDING_CONSULTANCY: [{ label: 'Building Consultancy', value: 'BUILDING_CONSULTANCY' }],
        RENOVATIONS_REMODELING_REPAIRS: [{ label: 'Renovations, Remodeling & Repairs', value: 'RENOVATIONS_REMODELING_REPAIRS' }]
    };
    consultancySubtags = [
        { label: 'Architecture', value: 'ARCHITECTURE' },
        { label: 'Costing', value: 'COSTING' },
        { label: 'Approvals', value: 'APPROVALS' },
        { label: 'Materials', value: 'MATERIALS' },
        { label: 'Electrical', value: 'ELECTRICAL' },
        { label: 'Plumbing', value: 'PLUMBING' },
        { label: 'Waterproofing', value: 'WATERPROOFING' },
        { label: 'Interior Design', value: 'INTERIOR_DESIGN' },
        { label: 'Bio Digester', value: 'BIO_DIGESTER' },
        { label: 'Landscaping', value: 'LANDSCAPING' }
    ];
    private displayNameMap: { [key: string]: string } = {
        JENGA_KWAKO: 'Jenga Kwako',
        JENGA_STRESS_FREE: 'Jenga Stress Free',
        LABOUR_ONLY_CONTRACTING: 'Labour Only Contracting',
        DIASPORA_BUILDING_SOLUTIONS: 'Diaspora Building Solutions',
        BUILDING_CONSULTANCY: 'Building Consultancy',
        RENOVATIONS_REMODELING_REPAIRS: 'Renovations, Remodeling & Repairs',
        JENGA_STRESS_FREE_COMMERCIAL: 'Commercial',
        JENGA_STRESS_FREE_RESIDENTIAL: 'Residential',
        LABOUR_ONLY_COMMERCIAL: 'Commercial',
        LABOUR_ONLY_RESIDENTIAL: 'Residential',
        DIASPORA_COMMERCIAL: 'Commercial',
        DIASPORA_RESIDENTIAL: 'Residential',
        ARCHITECTURE: 'Architecture',
        COSTING: 'Costing',
        APPROVALS: 'Approvals',
        MATERIALS: 'Materials',
        ELECTRICAL: 'Electrical',
        PLUMBING: 'Plumbing',
        WATERPROOFING: 'Waterproofing',
        INTERIOR_DESIGN: 'Interior Design',
        BIO_DIGESTER: 'Bio Digester',
        LANDSCAPING: 'Landscaping'
    };
    locationTypes = [
        { label: 'Kenya', value: 'KENYA' },
        { label: 'International', value: 'INTERNATIONAL' }
    ];
    genders = [
        { label: 'Male', value: 'Male' },
        { label: 'Female', value: 'Female' },
        { label: 'Other', value: 'Other' }
    ];
    contactMethods = [
        { label: 'Call', value: 'CALL' },
        { label: 'SMS', value: 'SMS' },
        { label: 'Email', value: 'EMAIL' },
        { label: 'WhatsApp', value: 'WHATSAPP' },
        { label: 'Agent', value: 'AGENT' },
    ];
    clientSource = [
        { label: 'Social Media', value: 'socialMedia' },
        { label: 'Walk-ins', value: 'walkins' },
        { label: 'Activations', value: 'activations' },
        { label: 'Website', value: 'website' },
        { label: 'Agent', value: 'agent' }
    ];
    kenyanCounties = kenyanCounties;
    allCountries = allCountries;
    countryCodes: any[] = countryCodes;
    lenders = lenderInstitutions;
    userRole!: string;

    constructor(
        private fb: FormBuilder,
        private clientService: ClientDetailsService,
        private messagesService: MessagesService,
        private userService: UserService,
    ) {}

    ngOnInit() {
        this.steps = [{ label: 'Personal Info' }, { label: 'Review' }];

        this.personalInfoForm = this.fb.group({
            firstName: ['', Validators.required],
            secondName: ['', Validators.required],
            surName: ['', Validators.required],
            email: ['', Validators.email],
            gender: ['', Validators.required],
            countryCode: ['+254'],
            phoneNumber: ['', [Validators.required, Validators.pattern(/^\+?\d{10,15}$/)]],
            locationType: ['', Validators.required],
            preferredContact: ['', Validators.required],
            clientSource: ['', Validators.required],
            projectName: ['', Validators.required],
            idNumber: [''],
            county: [''],
            country: [''],
            dob: [''],
            productOffering: ['', Validators.required],
            productTag: [''],
            bankName: [''],
            bankBranch: [''],
            consultancySubtags: [[]],
            followUpDate: ['', Validators.required],
            notes: [''],
            agentId: ['']
        });

        this.fetchAgents();
        this.onLocationChange();
        this.onProductOfferingChange();
        this.onClientSourceChange();
        this.loadDraft();
    }

    fetchAgents() {
        this.userService.getAgents().subscribe({
            next: (agents: User[]) => {
                this.agents = agents.map(agent => ({
                    label: `${agent.username} (${agent.email})`,
                    value: agent.id
                }));
            },
            error: (error) => {
                console.error('Error fetching agents:', error);
                this.messagesService.showError('Failed to load agents');
            }
        });
    }

    onClientSourceChange() {
        const clientSource = this.personalInfoForm.get('clientSource')?.value;
        if (clientSource === 'agent') {
            this.personalInfoForm.get('agentId')?.setValidators([Validators.required]);
        } else {
            this.personalInfoForm.get('agentId')?.clearValidators();
        }
        this.personalInfoForm.get('agentId')?.updateValueAndValidity();
    }

    onLocationChange() {
        const locationType = this.personalInfoForm.get('locationType')?.value;

        if (locationType === 'KENYA') {
            this.personalInfoForm.get('county')?.setValidators([Validators.required]);
            this.personalInfoForm.get('country')?.clearValidators();
        } else if (locationType === 'INTERNATIONAL') {
            this.personalInfoForm.get('country')?.setValidators([Validators.required]);
            this.personalInfoForm.get('county')?.clearValidators();
        } else {
            this.personalInfoForm.get('county')?.clearValidators();
            this.personalInfoForm.get('country')?.clearValidators();
        }

        this.personalInfoForm.get('county')?.updateValueAndValidity();
        this.personalInfoForm.get('country')?.updateValueAndValidity();
    }

    onProductOfferingChange() {
        const productOffering = this.personalInfoForm.get('productOffering')?.value;

        if (productOffering === 'JENGA_KWAKO') {
            this.personalInfoForm.get('bankName')?.setValidators([Validators.required]);
            this.personalInfoForm.get('bankBranch')?.setValidators([Validators.required]);
            this.personalInfoForm.get('consultancySubtags')?.clearValidators();
        } else if (productOffering === 'BUILDING_CONSULTANCY') {
            this.personalInfoForm.get('consultancySubtags')?.setValidators([Validators.required]);
            this.personalInfoForm.get('bankName')?.clearValidators();
            this.personalInfoForm.get('bankBranch')?.clearValidators();
        } else {
            this.personalInfoForm.get('bankName')?.clearValidators();
            this.personalInfoForm.get('bankBranch')?.clearValidators();
            this.personalInfoForm.get('consultancySubtags')?.clearValidators();
        }

        this.personalInfoForm.get('bankName')?.updateValueAndValidity();
        this.personalInfoForm.get('bankBranch')?.updateValueAndValidity();
        this.personalInfoForm.get('consultancySubtags')?.updateValueAndValidity();
        this.personalInfoForm.get('productTag')?.setValue('');
    }

    nextStep() {
        this.activeIndex++;
    }

    prevStep() {
        this.activeIndex--;
    }

    submitUserDetails() {
        this.loading = true;


        this.clientService.submitClientDetails(this.personalInfoForm.value).subscribe({
            next: () => {
                this.messagesService.showSuccess('Client details added successfully.');
                this.loading = false;
                this.activeIndex++;
            },
            error: (error) => {
                this.messagesService.showError('Error occurred while submitting client details.');
                console.log('System Error ', error);
                this.loading = false;
            }
        });
    }

    submitForm() {
        this.loading = true;
        this.idNumber = this.personalInfoForm.get('idNumber')?.value;
        if (this.personalInfoForm.get('productTag')?.value === '') {
            this.personalInfoForm.get('productTag')?.setValue('JENGA_KWAKO');
        }
        this.clientService.submitClientDetails(this.personalInfoForm.value).subscribe({
            next: () => {
                this.messagesService.showSuccess('Client onboarding completed successfully.');
                this.loading = false;
                this.personalInfoForm.reset();
                this.activeIndex = 0;
                this.clearDraft();
            },
            error: (error) => {
                this.messagesService.showError('Error occurred while submitting final client details.');
                console.error('Final Submission Error ', error);
                this.loading = false;
            }
        });
    }

    saveDraft() {
        if (this.activeIndex === 1) { // Only save draft on review page
            const draftData = {
                personalInfo: this.personalInfoForm.value,
                activeIndex: this.activeIndex
            };
            localStorage.setItem('clientOnboardingDraft', JSON.stringify(draftData));
            this.messagesService.showSuccess('Draft saved to local storage.');
        }
    }



    loadDraft() {
        const draft = localStorage.getItem('clientOnboardingDraft');
        if (draft) {
            const draftData = JSON.parse(draft);
            this.personalInfoForm.patchValue(draftData.personalInfo);
            this.activeIndex = draftData.activeIndex;
            this.messagesService.showInfo('Draft loaded successfully.');
        }
    }

    clearDraft() {
        localStorage.removeItem('clientOnboardingDraft');
    }

    @HostListener('window:beforeunload', ['$event'])
    onBeforeUnload() {
        this.saveDraft();
    }

    getDisplayName(enumValue: string | string[]): string {
        if (Array.isArray(enumValue)) {
            return enumValue.map((val) => this.displayNameMap[val] || val).join(', ');
        }
        return this.displayNameMap[enumValue] || enumValue;
    }
}
