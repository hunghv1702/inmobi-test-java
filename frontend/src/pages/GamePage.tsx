import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useGame } from '../hooks/useGame';
import { usePayment } from '../hooks/usePayment';
import { APP_CONFIG } from '../config/env.config';
import { Sparkles, Dices, ShoppingCart, AlertTriangle, Zap, CheckCircle2, XCircle } from 'lucide-react';

export const GamePage: React.FC = () => {
  const { user } = useAuth();
  const {
    selectedNumber,
    setSelectedNumber,
    isSpinning,
    displayNumber,
    lastResult,
    error,
    submitGuess,
  } = useGame();

  const { initiateCheckout, isProcessing: paymentLoading, paymentError } = usePayment();

  if (!user) return null;

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      {/* Title Header */}
      <div className="text-center mb-8">
        <h2 className="text-3xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 via-indigo-200 to-pink-500 mb-2">
          Guess The Lucky Number
        </h2>
        <p className="text-gray-400 text-sm max-w-md mx-auto">
          Select a number between <span className="text-cyan-400 font-bold font-mono">{APP_CONFIG.GAME.MIN_NUMBER}</span> and{' '}
          <span className="text-cyan-400 font-bold font-mono">{APP_CONFIG.GAME.MAX_NUMBER}</span>. Match the server secret number to score <span className="text-amber-400 font-bold">+1 point</span>!
        </p>
      </div>

      {/* Main Interactive Hub */}
      <div className="glass-panel p-6 sm:p-10 rounded-3xl border border-gray-800 shadow-2xl relative">
        {/* Out of Turns Alert */}
        {user.turns <= 0 && (
          <div className="mb-6 p-4 rounded-2xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <AlertTriangle className="w-6 h-6 text-amber-400 shrink-0" />
              <div>
                <h4 className="font-bold text-sm text-amber-300">No Turns Remaining</h4>
                <p className="text-xs text-amber-400/80">Get 5 more turns to keep playing and climbing the leaderboard.</p>
              </div>
            </div>
            <button
              onClick={initiateCheckout}
              disabled={paymentLoading}
              className="px-4 py-2 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs shadow-lg transition flex items-center gap-2 cursor-pointer shrink-0"
            >
              <ShoppingCart className="w-4 h-4" />
              <span>{paymentLoading ? 'Redirecting...' : `Buy 5 Turns (${APP_CONFIG.GAME.TURN_PACKAGE_COST})`}</span>
            </button>
          </div>
        )}

        {/* Error Banners */}
        {(error || paymentError) && (
          <div className="mb-6 p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-400 text-xs text-center font-mono">
            {error || paymentError}
          </div>
        )}

        {/* Number Reel / Server Slot */}
        <div className="flex flex-col items-center justify-center my-6">
          <div
            className={`w-32 h-32 rounded-3xl flex items-center justify-center text-5xl font-black font-mono border-2 shadow-2xl transition transform ${
              isSpinning
                ? 'bg-indigo-900/40 border-cyan-400 text-cyan-300 animate-pulse scale-105'
                : lastResult?.isCorrect
                ? 'bg-emerald-500/20 border-emerald-400 text-emerald-300 neon-border-cyan'
                : lastResult && !lastResult.isCorrect
                ? 'bg-rose-500/20 border-rose-400 text-rose-300'
                : 'bg-gray-900/90 border-gray-800 text-cyan-400'
            }`}
          >
            {displayNumber}
          </div>
          <span className="text-xs font-mono text-gray-500 mt-2 uppercase tracking-widest">
            {isSpinning ? 'Spinning Server Number...' : 'Server Secret Number'}
          </span>
        </div>

        {/* Number Pickers */}
        <div className="my-8">
          <label className="block text-center text-xs font-semibold text-gray-400 mb-3 uppercase tracking-widest font-mono">
            Select Your Guess Number
          </label>
          <div className="grid grid-cols-5 gap-2 sm:gap-4 max-w-lg mx-auto">
            {[1, 2, 3, 4, 5].map((num) => {
              const isSelected = selectedNumber === num;
              return (
                <button
                  key={num}
                  onClick={() => setSelectedNumber(num)}
                  disabled={isSpinning || user.turns <= 0}
                  className={`h-16 rounded-2xl font-black text-2xl font-mono transition transform active:scale-95 cursor-pointer flex items-center justify-center border ${
                    isSelected
                      ? 'bg-gradient-to-b from-cyan-500 to-indigo-600 text-white border-cyan-300 shadow-lg shadow-cyan-500/30 scale-105'
                      : 'bg-gray-900/60 border-gray-800 text-gray-400 hover:text-white hover:border-gray-700'
                  } disabled:opacity-40 disabled:cursor-not-allowed`}
                >
                  {num}
                </button>
              );
            })}
          </div>
        </div>

        {/* Submit Guess Button */}
        <div className="text-center max-w-sm mx-auto">
          <button
            onClick={submitGuess}
            disabled={isSpinning || selectedNumber === null || user.turns <= 0}
            className="w-full py-4 rounded-2xl bg-gradient-to-r from-cyan-500 via-indigo-600 to-pink-500 hover:from-cyan-400 hover:to-pink-400 text-white font-extrabold text-lg shadow-xl shadow-cyan-500/25 transition transform active:scale-95 flex items-center justify-center gap-3 cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed"
          >
            <Dices className={`w-6 h-6 ${isSpinning ? 'animate-spin' : ''}`} />
            <span>{isSpinning ? 'REEL SPINNING...' : 'SUBMIT GUESS'}</span>
          </button>
        </div>

        {/* Result Message Banner */}
        {lastResult && !isSpinning && (
          <div
            className={`mt-8 p-4 rounded-2xl border flex items-center justify-center gap-3 text-sm font-semibold transition ${
              lastResult.isCorrect
                ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
                : 'bg-rose-500/10 border-rose-500/30 text-rose-300'
            }`}
          >
            {lastResult.isCorrect ? (
              <>
                <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
                <span>Bingo! Server picked <strong>{lastResult.serverNumber}</strong>. You won <strong>+1 Point</strong>!</span>
              </>
            ) : (
              <>
                <XCircle className="w-5 h-5 text-rose-400 shrink-0" />
                <span>Better luck next time! Server picked <strong>{lastResult.serverNumber}</strong>, your guess was <strong>{lastResult.guess}</strong>.</span>
              </>
            )}
          </div>
        )}
      </div>

      {/* Stripe Payment CTA */}
      <div className="mt-8 glass-panel p-4 rounded-2xl border border-gray-800 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2 rounded-xl bg-cyan-500/20 text-cyan-400">
            <Zap className="w-5 h-5" />
          </div>
          <div>
            <h4 className="font-bold text-sm text-white">Need More Turns?</h4>
            <p className="text-xs text-gray-400">Instant turn package fulfillment powered by Stripe Checkout</p>
          </div>
        </div>
        <button
          onClick={initiateCheckout}
          disabled={paymentLoading}
          className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-bold text-xs shadow-lg transition flex items-center gap-2 cursor-pointer shrink-0"
        >
          <Sparkles className="w-4 h-4" />
          <span>{paymentLoading ? 'Redirecting to Stripe...' : `Buy 5 Turns (${APP_CONFIG.GAME.TURN_PACKAGE_COST})`}</span>
        </button>
      </div>
    </div>
  );
};
