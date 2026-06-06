import { useState } from 'react';
import ChatWindow from './components/ChatWindow';
import './App.css';

function App() {
  return (
    <div className="app">
      <header className="app-header">
        <div className="header-content">
          <div className="logo">
            <span className="logo-icon">🏢</span>
            <div>
              <h1>HR Assistant</h1>
              <p>Ask me anything about HR policies</p>
            </div>
          </div>
          <div className="status-badge">
            <span className="status-dot"></span>
            Online
          </div>
        </div>
      </header>
      <main className="app-main">
        <ChatWindow />
      </main>
    </div>
  );
}

export default App;
