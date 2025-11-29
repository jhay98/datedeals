import { Business } from './business.model';

export interface User {
  userId?: number;
  username: string;
  password?: string;
  role: 'ADMIN' | 'BUSINESS';
  business?: Business;
  enabled: boolean;
}

export interface UserRequest {
  username: string;
  password: string;
  role: 'ADMIN' | 'BUSINESS';
  businessId?: number;
  enabled: boolean;
}
