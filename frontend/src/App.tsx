import { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { usePayment } from './hooks/usePayment';
import { Navbar } from './components/Navbar';
import { LeaderboardModal } from './components/LeaderboardModal';
import { LoginPage } from './pages/LoginPage';
import { GamePage } from './pages/GamePage';
import { PaymentSuccessPage } from './pages/PaymentSuccessPage';
import { PaymentCancelPage } from './pages/PaymentCancelPage';

function AppContent() {
  const { isAuthenticated, isLoading, refreshProfile } = useAuth();
  const { initiateCheckout } = usePayment();
  const [leaderboardOpen, setLeaderboardOpen] = useState(false);

  const pathname = window.location.pathname;
  const isPaymentSuccess = pathname === '/payment/success';
  const isPaymentCancel = pathname === '/payment/cancel';

  const handleReturnToGame = () => {
    window.history.pushState({}, '', '/');
    refreshProfile();
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-black text-cyan-400 font-mono text-sm">
        Initializing Guess Royale Arcade...
      </div>
    );
  }

  if (isPaymentSuccess) {
    return <PaymentSuccessPage onReturnToGame={handleReturnToGame} />;
  }

  if (isPaymentCancel) {
    return <PaymentCancelPage onReturnToGame={handleReturnToGame} />;
  }

  if (!isAuthenticated) {
    return <LoginPage />;
  }

  return (
    <div className="min-h-screen bg-[#0b0f19] text-gray-100 flex flex-col font-sans">
      <Navbar
        onOpenBuyTurns={initiateCheckout}
        onOpenLeaderboard={() => setLeaderboardOpen(true)}
      />

      <main className="flex-1">
        <GamePage />
      </main>

      <LeaderboardModal
        isOpen={leaderboardOpen}
        onClose={() => setLeaderboardOpen(false)}
      />
    </div>
  );
}

export function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;
