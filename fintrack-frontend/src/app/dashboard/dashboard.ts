import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TransactionService, Transaction, Category, MonthlySummary } from '../services/transaction';
import { Auth } from '../services/auth';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatDialogModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {

  transactions: Transaction[] = [];
  categories: Category[] = [];
  currentSummary: MonthlySummary | null = null;
  showAddForm = false;
  loading = false;
  email = '';

  transactionForm: FormGroup;
  displayedColumns = ['date', 'category', 'description', 'type', 'amount', 'actions'];
  showAddCategory = false;
  categoryForm: FormGroup;

  constructor(
    private transactionService: TransactionService,
    private authService: Auth,
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private router: Router
  ) {
    this.transactionForm = this.fb.group({
      amount: ['', [Validators.required, Validators.min(0.01)]],
      description: [''],
      date: [new Date().toISOString().split('T')[0], Validators.required],
      type: ['EXPENSE', Validators.required],
      categoryId: ['', Validators.required]
    });
    this.categoryForm = this.fb.group({
      name: ['', Validators.required],
      type: ['EXPENSE', Validators.required]
    });
  }

  ngOnInit(): void {
    this.email = this.authService.getEmail() || '';
    this.loadData();
  }

  loadData(): void {
    this.loading = true;

    this.transactionService.getTransactions().subscribe({
      next: (data) => {
        this.transactions = data;
        this.loading = false;
      },
      error: (err) => {
        if (err.status === 401) this.router.navigate(['/auth/login']);
        this.loading = false;
      }
    });

    this.transactionService.getCategories().subscribe({
      next: (data) => this.categories = data
    });

    this.transactionService.getCurrentMonthSummary().subscribe({
      next: (data) => this.currentSummary = data,
      error: () => this.currentSummary = null
    });
  }

  get filteredCategories(): Category[] {
    const type = this.transactionForm.get('type')?.value;
    return this.categories.filter(c => c.type === type);
  }

  onSubmit(): void {
    if (this.transactionForm.invalid) return;

    this.transactionService.createTransaction(this.transactionForm.value).subscribe({
      next: () => {
        this.snackBar.open('Transaction added!', 'Close', { duration: 3000 });
        this.showAddForm = false;
        this.transactionForm.reset({
          type: 'EXPENSE',
          date: new Date().toISOString().split('T')[0]
        });
        this.loadData();
      },
      error: () => {
        this.snackBar.open('Failed to add transaction', 'Close', { duration: 3000 });
      }
    });
  }

  deleteTransaction(id: number): void {
    this.transactionService.deleteTransaction(id).subscribe({
      next: () => {
        this.snackBar.open('Transaction deleted', 'Close', { duration: 3000 });
        this.loadData();
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }

  getMonthName(month: number): string {
    return new Date(2024, month - 1).toLocaleString('default', { month: 'long' });
  }

  addCategory(): void {
    if (this.categoryForm.invalid) return;

    this.transactionService.createCategory(
      this.categoryForm.value.name,
      this.categoryForm.value.type
    ).subscribe({
      next: () => {
        this.snackBar.open('Category created!', 'Close', { duration: 3000 });
        this.showAddCategory = false;
        this.categoryForm.reset({ type: 'EXPENSE' });
        this.loadData();
      },
      error: (err) => {
        this.snackBar.open(err.error?.message || 'Failed to create category',
          'Close', { duration: 3000 });
      }
    });
  }
}