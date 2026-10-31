import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { TermsModal } from '../components/TermsModal';
import { Zap, ShieldCheck, History, ArrowRight, Flame } from 'lucide-react';

export const LandingPage: React.FC = () => {
  const { isAuthenticated, hasAgreedToTerms, setAgreedToTerms, loginAsGuest } = useAuth();
  const [isTermsOpen, setIsTermsOpen] = useState(false);
  const [isStarting, setIsStarting] = useState(false);
  const navigate = useNavigate();

  const handleStartChat = async () => {
    if (!hasAgreedToTerms) {
      setIsTermsOpen(true);
      return;
    }
    await proceedToChat();
  };

  const handleTermsConfirmed = async () => {
    setAgreedToTerms(true);
    setIsTermsOpen(false);
    await proceedToChat();
  };

  const proceedToChat = async () => {
    setIsStarting(true);
    try {
      if (!isAuthenticated) {
        await loginAsGuest();
      }
      navigate('/chat');
    } catch (err) {
      console.error('Failed to start chat', err);
    } finally {
      setIsStarting(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex flex-col items-center justify-center px-4 sm:px-6 lg:px-8 py-12 relative overflow-hidden">
      {/* Background ambient glowing orbs */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-indigo-600/15 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-80 h-80 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />

      <div className="max-w-4xl w-full text-center relative z-10">
        {/* Highlight pill */}
        <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-indigo-950/60 border border-indigo-500/30 text-indigo-300 text-xs sm:text-sm font-semibold mb-6 shadow-inner">
          <Flame className="w-4 h-4 text-amber-400" />
          <span>The Modern Random 1-to-1 Chat Experience</span>
        </div>

        {/* Hero headline */}
        <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight text-white mb-6 leading-tight">
          Talk to Strangers.<br />
          <span className="bg-gradient-to-r from-indigo-400 via-purple-300 to-cyan-400 bg-clip-text text-transparent">
            Completely Anonymous.
          </span>
        </h1>

        <p className="max-w-2xl mx-auto text-base sm:text-lg text-slate-300 mb-10 leading-relaxed">
          Connect in seconds with random people from all over the world. Chat anonymously as a guest or sign in to save your conversation histories.
        </p>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 max-w-md mx-auto mb-16">
          <button
            onClick={handleStartChat}
            disabled={isStarting}
            className="w-full sm:w-auto flex-1 flex items-center justify-center gap-2.5 px-8 py-4 rounded-2xl bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 text-white font-bold text-lg shadow-xl shadow-indigo-600/30 transition-all hover:scale-105 active:scale-95 cursor-pointer disabled:opacity-60"
          >
            <Zap className="w-5 h-5 fill-current" />
            <span>{isStarting ? 'Connecting...' : 'Start Chatting'}</span>
            <ArrowRight className="w-5 h-5 ml-1" />
          </button>

          {!isAuthenticated && (
            <button
              onClick={() => navigate('/login')}
              className="w-full sm:w-auto px-6 py-4 rounded-2xl bg-slate-900/80 hover:bg-slate-800 text-slate-200 font-semibold text-base border border-slate-700 transition-all hover:scale-105 active:scale-95"
            >
              Sign In to Save Chats
            </button>
          )}
        </div>

        {/* Feature Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-left">
          <div className="glass-card rounded-2xl p-6 transition-all hover:border-indigo-500/30">
            <div className="w-12 h-12 rounded-xl bg-indigo-500/10 text-indigo-400 flex items-center justify-center mb-4">
              <Zap className="w-6 h-6" />
            </div>
            <h2 className="text-lg font-bold text-white mb-2">Instant Matchmaking</h2>
            <p className="text-sm text-slate-400 leading-relaxed">
              Queue up with one click. Our smart in-memory queue pairs you with another waiting stranger in milliseconds.
            </p>
          </div>

          <div className="glass-card rounded-2xl p-6 transition-all hover:border-indigo-500/30">
            <div className="w-12 h-12 rounded-xl bg-cyan-500/10 text-cyan-400 flex items-center justify-center mb-4">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <h2 className="text-lg font-bold text-white mb-2">Total Anonymity</h2>
            <p className="text-sm text-slate-400 leading-relaxed">
              No personal data exposed. You are always labeled as "Stranger" to your partner with automated moderation.
            </p>
          </div>

          <div className="glass-card rounded-2xl p-6 transition-all hover:border-indigo-500/30">
            <div className="w-12 h-12 rounded-xl bg-purple-500/10 text-purple-400 flex items-center justify-center mb-4">
              <History className="w-6 h-6" />
            </div>
            <h2 className="text-lg font-bold text-white mb-2">Saved History Option</h2>
            <p className="text-sm text-slate-400 leading-relaxed">
              Prefer to keep memorable conversations? Registered users can browse, search, and delete transcripts anytime.
            </p>
          </div>
        </div>

        {/* Rules Footer Alert */}
        <div className="mt-14 p-4 rounded-xl bg-slate-900/40 border border-slate-800 text-xs text-slate-400 max-w-xl mx-auto">
          StrangerChat is strictly for ages <strong>18+</strong>. Unacceptable behavior, nudity, harassment, or spam will result in an immediate IP block.
        </div>
      </div>

      <TermsModal
        isOpen={isTermsOpen}
        onConfirm={handleTermsConfirmed}
        onCancel={() => setIsTermsOpen(false)}
      />
    </div>
  );
};
