import React, { useEffect, useState } from 'react';
import { gameApi } from '../api/gameApi';
import type { LeaderboardEntry } from '../types/api';
import { Trophy, X, Crown, RefreshCw } from 'lucide-react';

interface LeaderboardModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const LeaderboardModal: React.FC<LeaderboardModalProps> = ({ isOpen, onClose }) => {
  const [entries, setEntries] = useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const fetchLeaderboard = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await gameApi.getLeaderboard();
      setEntries(data.leaderboard || []);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load leaderboard');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      fetchLeaderboard();
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-fadeIn">
      <div className="glass-panel w-full max-w-md rounded-2xl overflow-hidden shadow-2xl border border-amber-500/20">
        <div className="flex items-center justify-between p-5 border-b border-gray-800 bg-gradient-to-r from-amber-500/10 via-transparent to-transparent">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-amber-500/20 text-amber-400">
              <Trophy className="w-5 h-5" />
            </div>
            <div>
              <h2 className="font-bold text-lg text-white">Global Leaderboard</h2>
              <p className="text-xs text-gray-400">Top 10 High Scorers</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={fetchLeaderboard}
              disabled={loading}
              className="p-2 text-gray-400 hover:text-amber-400 rounded-lg hover:bg-white/5 transition cursor-pointer"
              title="Refresh"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            </button>
            <button
              onClick={onClose}
              className="p-2 text-gray-400 hover:text-white rounded-lg hover:bg-white/5 transition cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        <div className="p-5 max-h-[60vh] overflow-y-auto">
          {loading ? (
            <div className="py-8 text-center text-gray-400 font-mono text-xs">Loading top scores...</div>
          ) : error ? (
            <div className="py-8 text-center text-rose-400 font-mono text-xs">{error}</div>
          ) : entries.length === 0 ? (
            <div className="py-8 text-center text-gray-400 font-mono text-xs">No high scores yet. Be the first!</div>
          ) : (
            <div className="space-y-2">
              {entries.map((entry, index) => {
                const rank = index + 1;
                const isTop1 = rank === 1;
                const isTop2 = rank === 2;
                const isTop3 = rank === 3;

                return (
                  <div
                    key={index}
                    className={`flex items-center justify-between p-3 rounded-xl border transition ${
                      isTop1
                        ? 'bg-amber-500/10 border-amber-500/40 text-amber-300 neon-border-gold'
                        : isTop2
                        ? 'bg-slate-300/10 border-slate-300/30 text-slate-200'
                        : isTop3
                        ? 'bg-amber-700/10 border-amber-700/30 text-amber-200'
                        : 'bg-gray-800/40 border-gray-800 text-gray-300'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <span className="w-6 text-center font-bold font-mono text-sm">
                        {isTop1 ? (
                          <Crown className="w-5 h-5 text-amber-400 inline" />
                        ) : (
                          `#${rank}`
                        )}
                      </span>
                      <span className="font-mono text-sm truncate max-w-[180px]">
                        {entry.email}
                      </span>
                    </div>
                    <span className="font-mono font-bold text-sm">
                      {entry.score} pts
                    </span>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
