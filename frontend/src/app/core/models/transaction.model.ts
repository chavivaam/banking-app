export type TransactionType = 'CREDIT' | 'DEBIT';

export interface TransactionDTO {
  id: number;
  userId: number;
  username: string;
  type: TransactionType;
  amount: number;
  description: string;
  createdDate: string;
}

export interface TransactionRequest {
  type: TransactionType;
  amount: number;
  description: string;
}
