import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import type { ConversationDetail } from '../types/chat';
import { ArrowLeft, Clock, MessageSquare, Trash2, Loader2, Calendar } from 'lucide-react';

export const ConversationDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { isAuthenticated, isGuest } = useAuth();
  const navigate = useNavigate();

  const [conversation, setConversation] = useState<ConversationDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated || isGuest) {
      navigate('/login');
      return;
    }

    if (id) {
      loadDetail(id);
    }
  }, [id, isAuthenticated, isGuest, navigate]);

  const loadDetail = async (convId: string) => {
    setIsLoading(true);
    try {
      const data = await api.getConversationDetail(convId);
      setConversation(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load conversation details');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!id) return;
    if (!confirm('Are you sure you want to delete this conversation transcript?')) return;

    try {
      await api.deleteConversation(id);
      navigate('/history');
    } catch (err: any) {
      alert(err.message || 'Failed to delete conversation');
    }
  };

  const formatDuration = (seconds: number) => {
    if (seconds < 60) return `${seconds} seconds`;
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}m ${secs}s`;
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      {/* Header bar */}
      <div className="flex items-center justify-between mb-6">
        <Link
          to="/history"
          className="inline-flex items-center gap-2 text-sm font-semibold text-slate-400 hover:text-white transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to History</span>
        </Link>

        {conversation && (
          <button
            onClick={handleDelete}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 text-xs font-semibold border border-rose-500/20 transition-colors"
          >
            <Trash2 className="w-3.5 h-3.5" />
            <span>Delete Transcript</span>
          </button>
        )}
      </div>

      {isLoading ? (
        <div className="py-20 flex flex-col items-center justify-center text-slate-400">
          <Loader2 className="w-8 h-8 animate-spin text-indigo-400 mb-3" />
          <span className="text-sm">Loading transcript...</span>
        </div>
      ) : error || !conversation ? (
        <div className="p-6 rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          {error || 'Conversation not found'}
        </div>
      ) : (
        <div className="space-y-4">
          {/* Metadata Card */}
          <div className="glass-panel rounded-2xl p-5 border border-white/10">
            <h1 className="text-xl font-bold text-white mb-2">
              Chat with {conversation.partnerLabel}
            </h1>
            <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400">
              <span className="flex items-center gap-1.5">
                <Calendar className="w-3.5 h-3.5 text-indigo-400" />
                {new Date(conversation.startedAt).toLocaleString()}
              </span>
              <span className="flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-cyan-400" />
                Duration: {formatDuration(conversation.durationSeconds)}
              </span>
              <span className="flex items-center gap-1.5">
                <MessageSquare className="w-3.5 h-3.5 text-purple-400" />
                {conversation.messages.length} messages
              </span>
            </div>
          </div>

          {/* Transcript Container */}
          <div className="glass-card rounded-2xl p-5 sm:p-6 space-y-4 min-h-[400px]">
            {conversation.messages.length === 0 ? (
              <div className="text-center py-12 text-slate-500 text-sm">
                No messages recorded in this conversation.
              </div>
            ) : (
              conversation.messages.map((msg) => (
                <div
                  key={msg.id}
                  className={`flex flex-col ${msg.fromMe ? 'items-end' : 'items-start'}`}
                >
                  <div className="flex items-center gap-1.5 mb-1 px-1">
                    <span
                      className={`text-[11px] font-bold ${
                        msg.fromMe ? 'text-indigo-400' : 'text-cyan-400'
                      }`}
                    >
                      {msg.senderLabel}
                    </span>
                    <span className="text-[10px] text-slate-500">
                      {new Date(msg.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>

                  <div
                    className={`max-w-[85%] sm:max-w-[75%] px-4 py-2.5 rounded-2xl text-sm leading-relaxed break-words shadow-md ${
                      msg.fromMe
                        ? 'bg-gradient-to-r from-indigo-600 to-indigo-500 text-white rounded-tr-none'
                        : 'bg-slate-800 text-slate-100 border border-slate-700/70 rounded-tl-none'
                    }`}
                    dangerouslySetInnerHTML={{ __html: msg.content }}
                  />
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};
