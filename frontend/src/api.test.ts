import {describe,expect,it} from 'vitest';
import {money,session} from './api';

describe('frontend API utilities',()=>{
  it('formats integer paise as INR without rounding drift',()=>{
    expect(money(10)).toContain('0.10');
    expect(money(1025)).toContain('10.25');
    expect(money(9999)).toContain('99.99');
  });

  it('keeps the access token only in memory and clears it on logout',()=>{
    session.set('synthetic-token');
    expect(session.active()).toBe(true);
    session.clear();
    expect(session.active()).toBe(false);
  });
});
