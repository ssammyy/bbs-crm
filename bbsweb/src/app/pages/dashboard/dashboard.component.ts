import { Component, OnInit } from '@angular/core';
import { AgentDashboardStats, DashboardService, DashboardStats } from './dashboard.service';
import { TableModule } from 'primeng/table';
import { Card } from 'primeng/card';
import { UIChart } from 'primeng/chart';
import { DatePipe, DecimalPipe, JsonPipe, NgClass, NgForOf, NgIf, TitleCasePipe } from '@angular/common';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Divider } from 'primeng/divider';
import { Button, ButtonDirective } from 'primeng/button';
import { Router, RouterLink } from '@angular/router';
import { Panel } from 'primeng/panel';
import { NgxSpinnerModule } from 'ngx-spinner';
import { NgxSpinnerService } from 'ngx-spinner';
import { UserGlobalService } from '../service/user.service';
import { Permissions } from '../data/permissions.enum';
import { Tooltip } from 'primeng/tooltip';
import { Client } from '../data/clietDTOs';
import { NotificationService } from '../../layout/service/notification.service';
import { Notification } from '../../shared/interfaces/notification.interface';
import { AgentApprovalService } from '../../services/agent-approval.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { FormsModule } from '@angular/forms';
import { ConfirmPopupModule } from 'primeng/confirmpopup';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { ClientMilestoneChecklistComponent } from './components/client-milestone-checklist/client-milestone-checklist.component';
import { UserService } from '../users/user.service';
@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [
        TableModule,
        Card,
        UIChart,
        DecimalPipe,
        DatePipe,
        TitleCasePipe,
        ProgressSpinner,
        NgIf,
        Button,
        NgxSpinnerModule,
        NgClass,
        Tooltip,
        NgForOf,
        ButtonDirective,
        DialogModule,
        InputTextModule,
        FormsModule,
        ConfirmPopupModule,
        ConfirmDialog,
        ClientMilestoneChecklistComponent
    ],
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.scss'],
    providers: [ConfirmationService, MessageService]
})
export class DashboardComponent implements OnInit {
    stats: DashboardStats | null = null;
    agentStats: AgentDashboardStats | null = null;
    clientStageChartData: any;
    clientSourceChartData: any;
    clearedVsUnclearedChartData: any;
    fileTypeChartData: any;
    revenueOverTimeChartData: any;
    clientsOverTimeChartData: any;
    agentRevenueOverTimeChartData: any;
    totalClients: number = 0;
    loading: boolean = false;
    agentLoading: boolean = false;
    userName: string = '';
    greeting: string = '';
    userRole!: string;
    notifications: Notification[] = [];
    unreadCount: number = 0;
    notificationLoading: boolean = false;
    pendingApprovals: any[] = [];
    selectedApproval: any = null;
    showApprovalModal = false;
    rejectionReason = '';
    clientId!: number

    clientSource = [
        { label: 'Social Media', value: 'socialMedia' },
        { label: 'Walkins', value: 'walkins' },
        { label: 'Activations', value: 'activations' },
        { label: 'Website', value: 'website' }
    ];

    chartOptions = {
        responsive: true,
        indexAxis: 'y',
        plugins: {
            legend: {
                position: 'top',
                labels: { font: { size: 14 } }
            },
            tooltip: {
                callbacks: {
                    label: (context: any) => {
                        const fullLabel = context.dataset.fullLabels[context.dataIndex];
                        const value = context.raw;
                        return `${fullLabel}: ${value} clients`;
                    }
                }
            }
        },
        scales: {
            x: {
                beginAtZero: true,
                title: { display: true, text: 'Number of Clients', font: { size: 14 } },
                ticks: { stepSize: 1, font: { size: 12 } },
                grid: { display: true }
            },
            y: {
                title: { display: true, text: 'Client Stages', font: { size: 14 } },
                ticks: { font: { size: 12 } },
                grid: { display: false }
            }
        }
    };

    clientSourceChartOptions = {
        responsive: true,
        plugins: {
            legend: {
                position: 'right',
                labels: { font: { size: 12 } }
            },
            tooltip: {
                callbacks: {
                    label: (context: any) => {
                        const label = context.label;
                        const value = context.raw;
                        const total = context.dataset.data.reduce((sum: number, val: number) => sum + val, 0);
                        const percentage = ((value / total) * 100).toFixed(1);
                        return `${label}: ${value} clients (${percentage}%)`;
                    }
                }
            }
        }
    };

    timeSeriesChartOptions = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top',
                labels: { font: { size: 12 } }
            },
            tooltip: {
                callbacks: {
                    label: (context: any) => {
                        const value = context.raw;
                        const label = context.dataset.label;
                        return `${label}: ${value.toLocaleString('en-KE', { style: 'currency', currency: 'KES' })}`;
                    }
                }
            }
        },
        scales: {
            x: {
                title: { display: true, text: 'Month', font: { size: 12 } },
                ticks: { font: { size: 10 }, maxRotation: 45, minRotation: 45 },
                grid: { display: false }
            },
            y: {
                beginAtZero: true,
                title: { display: true, text: 'Amount (KSH)', font: { size: 12 } },
                ticks: { font: { size: 10 } },
                grid: { display: true }
            }
        }
    };

    clientsOverTimeChartOptions = {
        responsive: true,
        plugins: {
            legend: {
                position: 'top',
                labels: { font: { size: 12 } }
            },
            tooltip: {
                callbacks: {
                    label: (context: any) => {
                        const value = context.raw;
                        return `Clients: ${value}`;
                    }
                }
            }
        },
        scales: {
            x: {
                title: { display: true, text: 'Month', font: { size: 12 } },
                ticks: { font: { size: 10 }, maxRotation: 45, minRotation: 45 },
                grid: { display: false }
            },
            y: {
                beginAtZero: true,
                title: { display: true, text: 'Number of Clients', font: { size: 12 } },
                ticks: { font: { size: 10 }, stepSize: 1 },
                grid: { display: true }
            }
        }
    };

    constructor(
        private dashboardService: DashboardService,
        private spinner: NgxSpinnerService,
        private userService: UserGlobalService,
        private router: Router,
        private notificationService: NotificationService,
        private agentApprovalService: AgentApprovalService,
        private confirmationService: ConfirmationService,
        private messageService: MessageService,
    ) {}

    ngOnInit(): void {
        this.getUserDetails();
        this.loadDashboardStats();
        this.loadNotifications();
        if (this.hasPrivilege(Permissions.VIEW_AGENT_DASHBOARD)) {
            this.loadAgentDashboardStats();
        }
        this.loadPendingApprovals();
        this.clientId = this.userService.getClientId();
        console.log('clientId', this.clientId);

    }


    loadNotifications(): void {
        this.notificationLoading = true;
        this.notificationService.getUserNotifications().subscribe({
            next: (notifications) => {
                this.notifications = notifications;
                this.notificationLoading = false;
            },
            error: (error) => {
                console.error('Error loading notifications:', error);
                this.notificationLoading = false;
            }
        });

        this.notificationService.getUnreadCount().subscribe({
            next: (count) => {
                this.unreadCount = count;
            },
            error: (error) => {
                console.error('Error loading unread count:', error);
            }
        });
    }

    markAsRead(notificationId: number): void {
        this.notificationService.markAsRead(notificationId).subscribe({
            next: () => {
                this.loadNotifications();
            },
            error: (error) => {
                console.error('Error marking notification as read:', error);
            }
        });
    }

    markAllAsRead(): void {
        this.notificationService.markAllAsRead().subscribe({
            next: () => {
                this.loadNotifications();
            },
            error: (error) => {
                console.error('Error marking all notifications as read:', error);
            }
        });
    }

    viewClient(client: Client): void {
        this.router.navigate(['app/pages/profile', client.id], { state: { client } });
    }

    loadDashboardStats(): void {
        this.spinner.show();
        this.loading = true;
        this.dashboardService.getDashboardStats().subscribe({
            next: (data) => {
                this.stats = data;
                if (this.stats?.clientStageDistribution) {
                    this.totalClients = Object.values(this.stats.clientStageDistribution).reduce((sum, count) => sum + (count || 0), 0);
                }
                this.prepareChartData();
                this.loading = false;
                this.spinner.hide();
            },
            error: (err) => {
                console.error('Error fetching dashboard stats:', err);
                this.loading = false;
                this.spinner.hide();
            }
        });
    }
    getUserDetails(): Promise<void> {
        return new Promise((resolve) => {
            this.userService.getDetails().subscribe({
                next: (response) => {
                    console.log('user role>>>> ', { response });
                    this.userRole = response?.role?.name;
                    this.userService.setRole(this.userRole);
                    const fullUsername = response?.username || '';
                    this.userName = fullUsername.split('@')[0].charAt(0).toUpperCase() + fullUsername.split('@')[0].slice(1);
                    this.updateGreeting();
                    resolve();
                },
                error: (error) => {
                    console.error('Error fetching user details:', error);
                    resolve();
                }
            });
        });
    }

    updateGreeting(): void {
        const hour = new Date().getHours();
        if (hour < 12) {
            this.greeting = 'Good Morning';
        } else if (hour < 18) {
            this.greeting = 'Good Afternoon';
        } else {
            this.greeting = 'Good Evening';
        }
    }

    loadAgentDashboardStats(): void {
        this.spinner.show('agentSpinner');
        this.agentLoading = true;
        this.dashboardService.getAgentDashboardStats().subscribe({
            next: (data) => {
                this.agentStats = data;
                this.prepareAgentChartData();
                this.agentLoading = false;
                this.spinner.hide('agentSpinner');
            },
            error: (err) => {
                console.error('Error fetching agent dashboard stats:', err);
                this.agentLoading = false;
                this.spinner.hide('agentSpinner');
            }
        });
    }

    prepareChartData(): void {
        if (!this.stats) return;

        const stageLabelMap: { [key: string]: string } = {
            REQUIREMENTS_GATHERING: 'Requirements',
            PROFORMA_INVOICE_GENERATION: 'Proforma Invoice',
            INVOICE_PENDING_DIRECTOR_APPROVAL: 'Pending Approval',
            PENDING_SITE_VISIT: 'Site Visit',
            GENERATE_SITE_VISIT_INVOICE: 'Site Visit Invoice',
            PENDING_CLIENT_SITE_VISIT_PAYMENT: 'Site Visit Payment',
            GENERATE_DRAWINGS_INVOICE: 'Drawings Invoice',
            PENDING_CLIENT_DRAWINGS_PAYMENT: 'Drawings Payment',
            GENERATE_BOQ_PREPARATION_INVOICE: 'BOQ Invoice',
            PENDING_CLIENT_BOQ_PREPARATION_PAYMENT: 'BOQ Payment',
            PENDING_CLIENT_BOQ_APPROVALS: 'BOQ Approvals'
        };

        const fullLabels = Object.keys(this.stats.clientStageDistribution);
        const shortLabels = fullLabels.map((label) => stageLabelMap[label] || label.replace(/_/g, ' '));
        this.clientStageChartData = {
            labels: shortLabels,
            datasets: [
                {
                    label: 'Clients per Stage',
                    data: Object.values(this.stats.clientStageDistribution),
                    backgroundColor: '#42A5F5',
                    borderColor: '#1E88E5',
                    borderWidth: 1,
                    fullLabels: fullLabels
                }
            ]
        };

        const sourceData = this.clientSource
            .filter((source) => this.stats?.clientSourceDistribution[source.value] !== undefined)
            .map((source) => ({
                label: source.label,
                value: this.stats!.clientSourceDistribution[source.value] || 0
            }))
            .filter((source) => source.value > 0);

        this.clientSourceChartData = {
            labels: sourceData.map((source) => source.label),
            datasets: [
                {
                    data: sourceData.map((source) => source.value),
                    backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0'],
                    hoverBackgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0']
                }
            ]
        };

        this.clearedVsUnclearedChartData = {
            labels: Object.keys(this.stats.clearedVsUnclearedBreakdown),
            datasets: [
                {
                    data: Object.values(this.stats.clearedVsUnclearedBreakdown),
                    backgroundColor: ['#4CAF50', '#F44336']
                }
            ]
        };

        this.fileTypeChartData = {
            labels: Object.keys(this.stats.fileStats.fileTypeBreakdown),
            datasets: [
                {
                    data: Object.values(this.stats.fileStats.fileTypeBreakdown),
                    backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0']
                }
            ]
        };

        this.revenueOverTimeChartData = {
            labels: this.stats.revenueOverTime.map((item) => item.period),
            datasets: [
                {
                    label: 'Revenue (KSH)',
                    data: this.stats.revenueOverTime.map((item) => item.value),
                    fill: false,
                    borderColor: '#42A5F5',
                    tension: 0.4
                }
            ]
        };

        this.clientsOverTimeChartData = {
            labels: this.stats.clientsOverTime.map((item) => item.period),
            datasets: [
                {
                    label: 'Clients',
                    data: this.stats.clientsOverTime.map((item) => item.value),
                    fill: false,
                    borderColor: '#FF6384',
                    tension: 0.4
                }
            ]
        };
    }

    prepareAgentChartData(): void {
        if (!this.agentStats) return;

        this.agentRevenueOverTimeChartData = {
            labels: this.agentStats.revenueOverTime.map((item) => item.period),
            datasets: [
                {
                    label: 'Revenue (KSH)',
                    data: this.agentStats.revenueOverTime.map((item) => item.value),
                    fill: false,
                    borderColor: '#42A5F5',
                    tension: 0.4
                }
            ]
        };
    }

    hasPrivilege(privilege: string) {
        return this.userService.hasPrivilege(privilege);
    }

    protected readonly Permissions = Permissions;

    navigateTo(route: string) {
        this.router.navigate([`app/pages/${route}`]);
    }

    loadPendingApprovals() {
        this.agentApprovalService.getPendingApprovals().subscribe({
            next: (approvals) => {
                this.pendingApprovals = approvals;
            },
            error: (error) => {
                console.error('Failed to load pending approvals:', error);
            }
        });
    }

    viewApprovalDetails(approval: any) {
        console.log('selected approval ', { approval });
        this.selectedApproval = approval;
        this.showApprovalModal = true;
    }

    approveAgent() {
        this.confirmationService.confirm({
            target: event?.target as EventTarget,
            message: 'Are you sure you want to approve this agent?',
            icon: 'pi pi-exclamation-triangle',
            accept: () => {
                this.agentApprovalService.approveAgent(this.selectedApproval.id).subscribe({
                    next: () => {
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Success',
                            detail: 'Agent approved successfully'
                        });
                        this.showApprovalModal = false;
                        this.loadPendingApprovals();
                    },
                    error: (error) => {
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error',
                            detail: error.error.message || 'Failed to approve agent'
                        });
                    }
                });
            }
        });
    }

    rejectAgent() {
        if (!this.rejectionReason) {
            this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: 'Please provide a reason for rejection'
            });
            return;
        }

        this.confirmationService.confirm({
            target: event?.target as EventTarget,
            message: 'Are you sure you want to reject this agent?',
            icon: 'pi pi-exclamation-triangle',
            accept: () => {
                this.agentApprovalService.rejectAgent(this.selectedApproval.id, this.rejectionReason).subscribe({
                    next: () => {
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Success',
                            detail: 'Agent rejected successfully'
                        });
                        this.showApprovalModal = false;
                        this.rejectionReason = '';
                        this.loadPendingApprovals();
                    },
                    error: (error) => {
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error',
                            detail: error.error.message || 'Failed to reject agent'
                        });
                    }
                });
            }
        });
    }
}
