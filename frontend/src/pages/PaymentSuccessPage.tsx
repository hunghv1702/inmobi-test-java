import React, { useEffect, useState } from 'react';
import { paymentApi } from '../api/paymentApi';
import confetti from 'canvas-confetti';
import { CheckCircle2, ShieldCheck, ArrowLeft, Loader2, Zap } from 'lucide-react';

interface PaymentSuccessPageProps {
  onReturnToGame: () => void;
}

export const PaymentSuccessPage: React.FC<PaymentSuccessPageProps> = ({ onReturnToGame }) => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [turnsAdded, setTurnsAdded] = useState<number | null>(null);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const sessionId = params.get('session_id');

    if (!sessionId) {
      setError('Checkout Session ID was not found in payment redirect');
      setLoading(false);
      return;
    }

    const confirm = async () => {
      try {
        const res = await paymentApi.confirmPayment(sessionId);
        setTurnsAdded(res.turns);
        setLoading(false);

        // Fire celebratory confetti!
        confetti({
          particleCount: 150,
          spread: 80,
          origin: { y: 0.5 },
          colors: ['#10B981', '#059669', '#34D399', '#FFD700'],
        });
      } catch (err: unknown) {
        setError(err instanceof Error ? err.message : 'Failed to confirm Stripe payment');
        setLoading(false);
      }
    };

    confirm();
  }, []);

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-radial from-slate-900 via-gray-950 to-black">
      <div className="w-full max-w-md glass-panel p-8 rounded-3xl border border-gray-800 text-center shadow-2xl">
        {loading ? (
          <div className="py-12 space-y-4">
            <Loader2 className="w-12 h-12 text-cyan-400 animate-spin mx-auto" />
            <h3 className="text-lg font-bold text-white">Confirming Payment...</h3>
            <p className="text-xs text-gray-400 font-mono">Verifying Stripe Checkout Session with Backend</p>
          </div>
        ) : error ? (
          <div className="py-8 space-y-4">
            <div className="w-16 h-16 rounded-full bg-rose-500/20 text-rose-400 flex items-center justify-center mx-auto border border-rose-500/30">
              <ShieldCheck className="w-8 h-8" />
            </div>
            <h3 className="text-xl font-bold text-rose-400">Payment Verification Issue</h3>
            <p className="text-xs text-gray-400 font-mono bg-rose-500/10 p-3 rounded-xl border border-rose-500/20">
              {error}
            </p>
            <button
              onClick={onReturnToGame}
              className="px-6 py-3 rounded-xl bg-gray-800 hover:bg-gray-700 text-white font-bold text-xs transition flex items-center justify-center gap-2 mx-auto cursor-pointer"
            >
              <ArrowLeft className="w-4 h-4" />
              <span>Back to Game Hub</span>
            </button>
          </div>
        ) : (
          <div className="py-8 space-y-6">
            <div className="w-20 h-20 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center mx-auto border border-emerald-500/40 shadow-xl shadow-emerald-500/20">
              <CheckCircle2 className="w-10 h-10" />
            </div>
            <div>
              <h2 className="text-2xl font-black text-white">Payment Successful!</h2>
              <p className="text-xs text-gray-400 mt-1 font-mono">
                Stripe transaction confirmed successfully
              </p>
            </div>

            <div className="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center gap-3">
              <Zap className="w-6 h-6 text-emerald-400 fill-emerald-400" />
              <span className="text-lg font-extrabold text-emerald-300 font-mono">
                +{turnsAdded ?? 5} TURNS CREDITED
              </span>
            </div>

            <button
              onClick={onReturnToGame}
              className="w-full py-3.5 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-bold text-sm shadow-lg shadow-emerald-500/25 transition transform active:scale-95 flex items-center justify-center gap-2 cursor-pointer"
            >
              <ArrowLeft className="w-4 h-4" />
              <span>Return to Game Hub & Play</span>
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
