import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Category {
  id: number;
  name: string;
  type: 'INCOME' | 'EXPENSE';
}

export interface Transaction {
  id: number;
  amount: number;
  description: string;
  date: string;
  type: 'INCOME' | 'EXPENSE';
  categoryName: string;
}

export interface MonthlySummary {
  year: number;
  month: number;
  totalIncome: number;
  totalExpense: number;
  netSavings: number;
  transactionCount: number;
}

export interface CreateTransactionRequest {
  amount: number;
  description: string;
  date: string;
  type: 'INCOME' | 'EXPENSE';
  categoryId: number;
}

@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.apiUrl}/api/transactions`);
  }

  createTransaction(request: CreateTransactionRequest): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.apiUrl}/api/transactions`, request);
  }

  deleteTransaction(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/api/transactions/${id}`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/api/categories`);
  }

  createCategory(name: string, type: 'INCOME' | 'EXPENSE'): Observable<Category> {
    return this.http.post<Category>(`${this.apiUrl}/api/categories`, { name, type });
  }

  getMonthlySummaries(): Observable<MonthlySummary[]> {
    return this.http.get<MonthlySummary[]>(`${this.apiUrl}/api/analytics/summary`);
  }

  getCurrentMonthSummary(): Observable<MonthlySummary> {
    const now = new Date();
    return this.http.get<MonthlySummary>(
      `${this.apiUrl}/api/analytics/summary/${now.getFullYear()}/${now.getMonth() + 1}`
    );
  }
}