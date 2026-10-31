import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import type { ConversationSummary } from '../types/chat';
import { History, Trash2, Clock, MessageSquare, ArrowRight, Loader2, Sparkles } from 'lucide-react';

export const HistoryPage: React.FC = () => {
  const { isAuthenticated, isGuest } = useAuth();
  const navigate = useNavigate();

  const [conversations, setConversations] = useState<ConversationSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated || isGuest) {
      navigate('/login');
      return;
    }

    loadHistory();
  }, [isAuthenticated, isGuest, navigate]);

  const loadHistory = async () => {
    setIsLoading(true);
    try {
      const data = await api.getConversations(0, 50);
      setConversations(data.content || []);
    } catch (err: any) {
      setError(err.message || 'Failed to load conversation history');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async (e: React.MouseEvent, id: string) => {
    e.stopPropagation();
    e.preventDefault();
    if (!confirm('Are you sure you want to remove this chat from your history?')) return;

    try {
      await api.deleteConversation(id);
      setConversations((prev) => prev.filter((c) => c.id !== id));
    } catch (err: any) {
      alert(err.message || 'Failed to delete conversation');
    }
  };

  const formatDuration = (seconds: number) => {
    if (seconds < 60) return `${seconds}s`;
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}m ${secs}s`;
  };

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-extrabold text-white flex items-center gap-2.5">
            <History className="w-8 h-8 text-indigo-400" />
            <span>My Chat History</span>
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Browse and review your past conversations with strangers.
          </p>
        </div>

        <Link
          to="/chat"
          className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-sm shadow-md transition-all hover:scale-105"
        >
          New Chat
        </Link>
      </div>

      {isLoading ? (
        <div className="py-20 flex flex-col items-center justify-center text-slate-400">
          <Loader2 className="w-8 h-8 animate-spin text-indigo-400 mb-3" />
          <span className="text-sm">Loading conversations...</span>
        </div>
      ) : error ? (
        <div className="p-4 rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          {error}
        </div>
      ) : conversations.length === 0 ? (
        <div className="glass-card rounded-3xl p-12 text-center">
          <div className="w-14 h-14 rounded-2xl bg-slate-800 text-slate-400 flex items-center justify-center mx-auto mb-4">
            <MessageSquare className="w-7 h-7" />
          </div>
          <h2 className="text-lg font-bold text-white mb-2">No Saved Chats Yet</h2>
          <p className="text-sm text-slate-400 max-w-md mx-auto mb-6">
            Whenever you chat while logged in, your conversations will be securely stored here so you can revisit them anytime.
          </p>
          <Link
            to="/chat"
            className="inline-flex items-center gap-2 px-6 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-sm shadow-lg transition-transform hover:scale-105"
          >
            <Sparkles className="w-4 h-4" />
            <span>Start Your First Chat</span>
          </Link>
        </div>
      ) : (
        <div className="space-y-3">
          {conversations.map((conv) => (
            <Link
              key={conv.id}
              to={`/history/${conv.id}`}
              className="glass-card rounded-2xl p-4 sm:p-5 flex items-center justify-between gap-4 hover:border-indigo-500/40 hover:bg-white/[0.03] transition-all group block cursor-pointer"
            >
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-3 mb-1.5">
                  <span className="font-bold text-sm text-white group-hover:text-indigo-300 transition-colors">
                    Chat with {conv.partnerLabel}
                  </span>
                  <span className="text-xs text-slate-500 flex items-center gap-1">
                    <Clock className="w-3 h-3" />
                    {new Date(conv.startedAt).toLocaleDateString()} at{' '}
                    {new Date(conv.startedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                  <span className="text-[11px] px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700/60">
                    {formatDuration(conv.durationSeconds)}
                  </span>
                </div>

                <p className="text-xs text-slate-400 truncate max-w-xl">
                  {conv.lastMessagePreview || 'No messages'}
                </p>
              </div>

              <div className="flex items-center gap-2">
                <button
                  onClick={(e) => handleDelete(e, conv.id)}
                  className="p-2 rounded-xl text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
                  title="Delete chat from history"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
                <ArrowRight className="w-4 h-4 text-slate-500 group-hover:text-indigo-400 group-hover:translate-x-1 transition-all" />
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};
