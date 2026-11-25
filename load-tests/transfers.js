import http from 'k6/http';import{check,sleep}from'k6';import{Counter}from'k6/metrics';
export const options={vus:10,duration:'20s',thresholds:{http_req_duration:['p(95)<1000'],http_req_failed:['rate<0.05']}};
const unexpected=new Counter('unexpected_errors');
export function setup(){const base=__ENV.BASE_URL||'http://localhost:8080/api/v1';const login=(email)=>http.post(`${base}/auth/login`,JSON.stringify({email,password:__ENV.DEMO_PASSWORD||'Demo@123'}),{headers:{'Content-Type':'application/json'}}).json('accessToken');const recipient=http.get(`${base}/recipients/lookup?phone=9876500002`,{headers:{Authorization:`Bearer ${login('rahul@payflow.local')}`}}).json('recipientId');return{base,token:login('rahul@payflow.local'),recipient};}
export default function(data){const response=http.post(`${data.base}/transfers`,JSON.stringify({recipientId:data.recipient,amount:'1.00',note:'k6 bounded load'}),{headers:{'Content-Type':'application/json',Authorization:`Bearer ${data.token}`,'Idempotency-Key':`${__VU}-${__ITER}-${Date.now()}`}});check(response,{'completed or expected insufficient funds':r=>r.status===200||r.status===422})||unexpected.add(1);sleep(.2);}

