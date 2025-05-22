import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css'; // Sizin genel stilleriniz
import App from './App';
import reportWebVitals from './reportWebVitals';
import { BrowserRouter } from 'react-router-dom';

// Bootstrap CSS (Eğer projenizde Bootstrap stillerini kullanıyorsanız ve App.js gibi başka bir yerde import edilmiyorsa)
import 'bootstrap/dist/css/bootstrap.min.css';

// Bootstrap Icons CSS (BU SATIRI EKLEDİK)
import 'bootstrap-icons/font/bootstrap-icons.css';


const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();