const base = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1';
let token: string | null = null;
export const session={set:(v:string)=>token=v,clear:()=>token=null,active:()=>!!token};
export async function api<T>(path:string,options:RequestInit={},idempotencyKey?:string):Promise<T>{
  const headers:Record<string,string>={'Content-Type':'application/json',...(options.headers as Record<string,string>)};
  if(token)headers.Authorization=`Bearer ${token}`;if(idempotencyKey)headers['Idempotency-Key']=idempotencyKey;
  const response=await fetch(base+path,{...options,headers});
  if(response.status===401){session.clear();window.dispatchEvent(new Event('payflow:unauthorized'));}
  const body=await response.json().catch(()=>({message:'Unable to reach PayFlow'}));
  if(!response.ok)throw new Error(body.message??'Request failed');return body;
}
export type Wallet={accountHolder:string;walletReference:string;balancePaise:number;currency:string};
export type Tx={reference:string;type:string;direction:'INCOMING'|'OUTGOING';amountPaise:number;status:string;note?:string;createdAt:string;counterparty:string};
export const money=(paise:number)=>new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR'}).format(paise/100);

