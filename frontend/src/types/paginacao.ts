export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number; // current page number
  size: number; // page size
  first: boolean;
  last: boolean;
  empty: boolean;
}
