import { useState } from 'react';
import { gameApi } from '../api/gameApi';
import { APP_CONFIG } from '../config/env.config';
import { useAuth } from '../context/AuthContext';
import confetti from 'canvas-confetti';
import type { GuessResponse } from '../types/api';

export const useGame = () => {
  const { user, updateUser } = useAuth();
  const [selectedNumber, setSelectedNumber] = useState<number | null>(
    APP_CONFIG.GAME.DEFAULT_SELECTION
  );
  const [isSpinning, setIsSpinning] = useState(false);
  const [displayNumber, setDisplayNumber] = useState<number | string>('?');
  const [lastResult, setLastResult] = useState<GuessResponse | null>(null);
  const [error, setError] = useState('');

  const submitGuess = async () => {
    if (selectedNumber === null) {
      setError(`Please select a number between ${APP_CONFIG.GAME.MIN_NUMBER} and ${APP_CONFIG.GAME.MAX_NUMBER}`);
      return;
    }
    if (!user || user.turns <= 0) {
      setError('You are out of turns! Purchase additional turns to continue playing.');
      return;
    }

    setError('');
    setIsSpinning(true);
    setLastResult(null);

    const interval = setInterval(() => {
      setDisplayNumber(Math.floor(Math.random() * APP_CONFIG.GAME.MAX_NUMBER) + 1);
    }, 80);

    try {
      const result = await gameApi.guess(selectedNumber);

      setTimeout(() => {
        clearInterval(interval);
        setDisplayNumber(result.serverNumber);
        setIsSpinning(false);
        setLastResult(result);

        updateUser({
          ...user,
          score: result.score,
          turns: result.turns,
        });

        if (result.isCorrect) {
          confetti({
            particleCount: 120,
            spread: 70,
            origin: { y: 0.6 },
            colors: ['#00F2FE', '#4FACFE', '#FFD700', '#EC4899'],
          });
        }
      }, 700);
    } catch (err: unknown) {
      clearInterval(interval);
      setIsSpinning(false);
      setDisplayNumber('?');
      setError(err instanceof Error ? err.message : 'Failed to submit guess');
    }
  };

  return {
    selectedNumber,
    setSelectedNumber,
    isSpinning,
    displayNumber,
    lastResult,
    error,
    submitGuess,
  };
};
