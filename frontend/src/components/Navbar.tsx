import React from 'react';
import { useAuth } from '../context/AuthContext';
import { APP_CONFIG } from '../config/env.config';
import { Trophy, Zap, LogOut, ShoppingCart, ShieldCheck } from 'lucide-react';

interface NavbarProps {
  onOpenBuyTurns: () => void;
  onOpenLeaderboard: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ onOpenBuyTurns, onOpenLeaderboard }) => {
  const { user, logout } = useAuth();

  return (
    <header className="sticky top-0 z-40 glass-panel border-b border-gray-800 px-4 py-3 sm:px-8">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        {/* Brand Logo */}
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-cyan-500/30">
            <ShieldCheck className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="font-extrabold text-xl tracking-wider text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 via-indigo-300 to-pink-500 neon-text-cyan">
              GUESS ROYALE
            </h1>
            <p className="text-[10px] text-gray-400 font-mono tracking-widest uppercase">
              Enterprise Gaming Platform
            </p>
          </div>
        </div>

        {/* User Stats & Actions */}
        {user && (
          <div className="flex items-center gap-3 sm:gap-5">
            {/* Score Badge */}
            <button
              onClick={onOpenLeaderboard}
              className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-amber-500/10 border border-amber-500/30 text-amber-400 hover:bg-amber-500/20 transition cursor-pointer"
              title="View Leaderboard"
            >
              <Trophy className="w-4 h-4 text-amber-400 animate-pulse" />
              <span className="font-bold text-sm font-mono">{user.score} pts</span>
            </button>

            {/* Turns Badge */}
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-cyan-500/10 border border-cyan-500/30 text-cyan-400">
              <Zap className="w-4 h-4 text-cyan-400 fill-cyan-400" />
              <span className="font-bold text-sm font-mono">{user.turns} turns</span>
            </div>

            {/* Buy Turns Button */}
            <button
              onClick={onOpenBuyTurns}
              className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-semibold text-xs shadow-md shadow-emerald-500/20 transition transform hover:-translate-y-0.5 cursor-pointer"
            >
              <ShoppingCart className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">Get 5 Turns</span> ({APP_CONFIG.GAME.TURN_PACKAGE_COST})
            </button>

            {/* User Account / Logout */}
            <div className="flex items-center gap-2 pl-2 border-l border-gray-800">
              <span className="hidden md:inline text-xs font-mono text-gray-400 truncate max-w-[120px]">
                {user.email}
              </span>
              <button
                onClick={logout}
                className="p-1.5 rounded-lg text-gray-400 hover:text-rose-400 hover:bg-rose-500/10 transition cursor-pointer"
                title="Logout"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>
    </header>
  );
};
